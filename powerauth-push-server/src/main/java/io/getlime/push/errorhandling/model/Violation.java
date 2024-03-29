/*
 * Copyright 2023 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.push.errorhandling.model;

/**
 * Entity class representing a violation of constraints.
 *
 * @author Petr Dvorak, petr@wultra.com
 *
 * @param fieldName Name of the field causing the violation.
 * @param invalidValue Value that was provided and was invalid.
 * @param hint Hing on what to do to fix the issue.
 */
public record Violation(String fieldName, Object invalidValue, String hint) {
}