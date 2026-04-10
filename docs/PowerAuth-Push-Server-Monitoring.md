# PowerAuth Push Server Monitoring

The PowerAuth Push Server is instrumented for monitoring and observability. It exposes:

- Prometheus-compatible metrics
- Application logs with distributed tracing correlation
- Standard Spring Boot health and probe endpoints

Observability is provided by the Spring Boot framework with support for Prometheus. All standard configuration options and built-in metrics are available. These capabilities are available out of the box in all environments; only integration with your monitoring stack (Prometheus, logging, Kubernetes, etc.) is required. Optionally, you can connect PowerAuth Push Server to an observability platform supporting OpenTelemetry (OTEL).

---

## Health Checks & Probes

The service exposes standard Spring Boot Actuator health endpoints for use by load balancers and orchestration platforms (such as Kubernetes). Only the basic health endpoint is enabled by default.

### Endpoints

- Overall health  
  `GET /powerauth-push-server/actuator/health`
- Liveness probe  
  `GET /powerauth-push-server/actuator/health/liveness`
- Readiness probe  
  `GET /powerauth-push-server/actuator/health/readiness`

### Configuration

Set the following configuration properties (e.g., via environment variables or `application.properties`) to enable the respective probes:

```properties
management.health.probes.enabled=true          # enable both liveness and readiness
management.health.livenessState.enabled=true   # enable liveness only
management.health.readinessState.enabled=true  # enable readiness only
```
<!-- begin warning info -->
Note: management.health.probes.enabled=true automatically enables both liveness and readiness endpoints. The other two can be used for fine-grained control if needed.
<!-- end -->

## Metrics (Prometheus)

The service exposes runtime metrics in Prometheus format via Spring Boot Actuator using the plain-text Prometheus exposition format.

Endpoints:

- Prometheus format
  - `GET /powerauth-push-server/actuator/prometheus`
- Spring Boot JSON format
  - `GET /powerauth-push-server/actuator/metrics`

Configuration:

Set the following configuration properties to enable metric endpoints and their exposure over HTTP:

```properties
management.endpoint.metrics.enabled=true  
management.endpoint.prometheus.enabled=true
management.endpoints.web.exposure.include=info,health,metrics,prometheus   # for full exposure of info, health and metrics endpoints
management.prometheus.metrics.export.enabled=true
```

## Security

The metrics and health endpoints are intended for internal access only. Expose them only on internal networks or behind an API gateway / service mesh, according to infrastructure security rules.

### Management Port Isolation

The management (Actuator) endpoints can be exposed on a dedicated port to separate them from the standard application traffic. When a separate port is configured, Actuator endpoints will be available directly under the /actuator context (i.e., without /powerauth-push-server).

```
management.server.port=9000  
```

## Other Monitoring Options

PowerAuth Push Server can be monitored via other tools as well. Example configuration to enable monitoring via OpenTelemetry:

1) Mount the Open Telemetry Java agent to directory `/app/config/`.
2) Configure the following environment variables:

```properties
# Java options to enable the OpenTelemetry agent (example for Tomcat)
JAVA_OPTS=-javaagent:/app/config/opentelemetry-javaagent.jar -Dotel.jmx.target.system=tomcat

# OpenTelemetry environment variables (with example values):

# The OTLP endpoint to which traces/metrics are exported
OTEL_EXPORTER_OTLP_ENDPOINT=https://otel-collector.example.com:4317

# Optional: Custom headers for authentication or other purposes
OTEL_EXPORTER_OTLP_HEADERS=Authorization=Bearer <token>

# Optional: Comma-separated list of HTTP request headers to capture
OTEL_INSTRUMENTATION_HTTP_SERVER_CAPTURE_REQUEST_HEADERS=x-request-id,x-b3-traceid

# Resource attributes describing the service (e.g., environment, region)
OTEL_RESOURCE_ATTRIBUTES=deployment.environment=prod,region=eu-central-1

# The logical service name for traces
OTEL_SERVICE_NAME=powerauth-push-server
```

## Application Logging & Distributed Tracing

The service produces structured application logs and participates in distributed tracing using the W3C Trace Context standard. Logs are written to standard output (stdout), which is suitable for containerized environments.

Each log entry is enriched (when tracing is active) with correlation identifiers, typically:

- `traceId` – ID of the distributed trace
- `spanId` – ID of the current span within that trace

These fields allow correlating log messages across multiple services that participate in the same request.

### Tracing (W3C traceparent)

The service supports the W3C traceparent header for incoming and outgoing HTTP calls to allow end-to-end request inspection and performance analysis.

For incoming requests:

- If traceparent (or compatible headers) are present, the service joins the existing trace.
- If no trace headers are present, the service starts a new trace.

For outgoing requests:

- HTTP clients used by the service automatically propagate the current trace context to downstream systems.

By default, the service accepts multiple tracing header formats (W3C, B3, B3 multi) and uses the W3C format for outgoing headers.

Configuration:
You can change which header formats are consumed and produced by setting the following properties:

```properties
# Accept multiple header formats
management.tracing.propagation.consume=B3,B3_MULTI,W3C
# Produce B3 format instead of W3C (example)
management.tracing.propagation.produce=B3
```

## Monitoring Targets

This section describes what should be monitored when operating the PowerAuth Push Server in production and how it maps to the metrics **exposed by the server itself** (via Spring Boot / Micrometer / Prometheus).

> Note: Metric names below are the defaults commonly produced by Spring Boot + Micrometer Prometheus registry. Exact names may differ slightly depending on framework versions and configuration.

---

### Resource Utilization

#### CPU Utilization

Monitor CPU usage per instance to detect overloads or misbehaving deployments.

**What to watch**

- Average CPU utilization per pod/instance.
- Sustained high CPU (e.g., ~80% or more).
- Sudden spikes correlated with increased traffic or specific operations.

**Metrics produced by the server**

- `process_cpu_usage`  
  Fraction of CPU used by the JVM process (0.0–1.0).
- `system_cpu_usage`  
  Overall system CPU usage on the node (if exposed).

---

#### Memory Utilization

Monitor memory to catch leaks and OOM situations before they happen.

**What to watch**

- Overall memory usage close to container limits.
- JVM heap usage and its trend over time.
- Frequent or long garbage collection pauses (if exposed).

**Metrics produced by the server**

- `jvm_memory_used_bytes{area="heap"}`
- `jvm_memory_max_bytes{area="heap"}`
- `jvm_gc_pause_seconds_max`
- `jvm_gc_pause_seconds_sum`
- `jvm_gc_pause_seconds_count`

It is useful to watch **max** values and trends over time. Percentiles for GC pause duration (if configured and exposed) can also be used to monitor the worst-case behaviour.

---

### Database Connection Pool (HikariCP)

PowerAuth Push Server uses a JDBC connection pool **HikariCP**. Monitoring the pool is critical to detect database saturation and connection issues.

#### Pool Utilization

**What to watch**

- How close the pool is to its configured maximum size.
- Whether there are threads waiting for a connection.
- Whether acquiring a connection is becoming slow or timing out.

**Metrics produced by the server**

- `hikaricp_connections{pool="HikariPool-Push-Server", state="active"}`
- `hikaricp_connections{pool="HikariPool-Push-Server", state="idle"}`
- `hikaricp_connections{pool="HikariPool-Push-Server", state="pending"}`
- `hikaricp_connections{pool="HikariPool-Push-Server", state="max"}`

Key signals:

- `state="active"` regularly close to `state="max"` → pool saturation.
- `state="pending"` consistently > 0 → threads are waiting for DB connections.

---

#### Connection Acquisition Time / Timeouts

**What to watch**

- Time needed to obtain a connection from the pool.
- Maximum observed acquisition times.
- Number of timeouts when acquiring a connection.

**Metrics produced by the server**

- `hikaricp_connections_acquire_seconds_sum{pool="HikariPool-Push-Server"}`
- `hikaricp_connections_acquire_seconds_count{pool="HikariPool-Push-Server"}`
- `hikaricp_connections_acquire_seconds_max{pool="HikariPool-Push-Server"}`

Use:

- `*_max` to see how bad the slowest acquisitions are in recent intervals.
- The combination of `*_sum` and `*_count` to understand general acquisition time behaviour.
- If histogram buckets are configured for this timer, percentiles (p95/p99 acquisition time) are good indicators of waiting time under load, but how to compute them depends on your monitoring stack.

Timeouts are typically visible via:

- Specific HikariCP timeout metrics (if enabled), and/or
- Increased HTTP 5xx responses for endpoints that perform database operations.

---

### API Performance & Reliability

API behaviour reflects user-facing health. Use HTTP server metrics to monitor throughput, error rate, and latency.

#### Traffic / Throughput

**What to watch**

- Requests per second overall and for key API endpoints.
- Unexpected drop to zero requests during normal operating hours.
- Sudden spikes that may overload the service or downstream systems.

**Metrics produced by the server**

(Exact labels may vary.)

- `http_server_requests_seconds_count{...}`

Common labels include:

- `uri` – endpoint pattern (e.g., `/...`)
- `method` – HTTP method (GET, POST, …)
- `status` – HTTP status code
- `outcome` – `SUCCESS`, `CLIENT_ERROR`, `SERVER_ERROR`, etc.
- `application` / `service` – `powerauth-push-server`

---

#### Success Rate (Error Rate)

**What to watch**

- Ratio of successful responses (2xx) vs. server errors (5xx).
- Error rate per key endpoint (e.g., onboarding/decision APIs).

**Metrics produced by the server**

Use the same `http_server_requests_seconds_count` metric filtered by labels:

- Successful responses:  
  `http_server_requests_seconds_count{outcome="SUCCESS", ...}`
- Server errors:  
  `http_server_requests_seconds_count{outcome="SERVER_ERROR", ...}`

Monitoring **proportion of server errors** vs. total requests is recommended. Percentiles are not needed here; focus on counts and relative ratios over time.

---

#### Response Time / Latency

**What to watch**

- Response times (median and tail behaviour) overall and for key endpoints.
- Latency spikes, especially for operations involving the database or external systems.

**Metrics produced by the server**

- `http_server_requests_seconds_sum{...}`
- `http_server_requests_seconds_count{...}`
- `http_server_requests_seconds_max{...}`

These metrics describe how long requests take in total and the maximum observed duration in a given period. If HTTP request histograms are enabled, percentiles (p95/p99 latency) are very useful to monitor “worst case” performance of the API, especially under load.

---

### Health & Availability

Use the Actuator health endpoints in combination with metrics to see if the service is able to accept traffic.

**What to watch**

- Status returned by:
    - `/powerauth-push-server/actuator/health`
    - `/powerauth-push-server/actuator/health/liveness`
    - `/powerauth-push-server/actuator/health/readiness`
- Frequency of transitions to `DOWN` or `OUT_OF_SERVICE`.
- Pod restarts and probe failures (usually observed via Kubernetes/platform metrics rather than application metrics).

While the health endpoints themselves are not Prometheus metrics, they are part of the overall monitoring picture and should be correlated with:

- Resource metrics (CPU, memory),
- Database pool metrics (HikariCP),
- API metrics (HTTP request counts, errors, latency),

to diagnose issues affecting PowerAuth Push Server’s availability and performance.

---

### Log Volume & Severity

In addition to raw logs in the central logging system, PowerAuth Push Server can expose **log event counters** as metrics. Monitoring the rate of log messages at different severity levels helps to detect problems early:

- A rise in **ERROR** logs can indicate internal failures or bugs in the server.
- A rise in **WARN** logs often indicates issues in **external systems** (downstream services, databases, message brokers) that are affecting PowerAuth Push Server, even if the server is still partially functioning.

<!-- begin info -->
Note: The exact metric names and availability depend on the logging metrics binder being enabled (e.g., Micrometer Logback metrics). The names below assume the standard Spring Boot + Micrometer + Logback setup.
<!-- end -->

**What to watch**

- Number and rate of **ERROR** and **WARN** log events over time.
- Sudden spikes in ERROR-level logs (potential incident).
- Gradual or repeated increase in WARN-level logs, especially when correlated with external dependency problems (timeouts, connection issues, etc.).

**Metrics produced by the server**

Typical logging metrics:

- `logback_events_total{level="ERROR"}`
- `logback_events_total{level="WARN"}`

Key signals:

- A sustained increase in `logback_events_total{level="ERROR"}` indicates that the server is frequently encountering errors and may require immediate investigation.
- An elevated or gradually increasing `logback_events_total{level="WARN"}` may indicate that some external component (external API, message broker) is unstable or misconfigured, and the server is compensating but not yet failing hard.
- Large changes in the ratio of ERROR/WARN logs to INFO logs can be used as an additional early-warning indicator.

These log-level metrics should be monitored together with other metrics to understand whether the problem is internal to the server or caused by external dependencies.

