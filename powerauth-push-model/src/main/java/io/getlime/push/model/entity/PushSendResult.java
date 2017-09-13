/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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

package io.getlime.push.model.entity;

/**
 * Class that contains push message sending result data.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PushSendResult {

    public static class iOS {

        private int sent;
        private int failed;
        private int total;

        /**
         * Get number of messages that were sent successfully.
         * @return Number of sent messages.
         */
        public int getSent() {
            return sent;
        }

        /**
         * Set number of messages that were sent successfully.
         * @param sent Number of sent messages.
         */
        public void setSent(int sent) {
            this.sent = sent;
        }

        /**
         * Get number of messages that were sent with failure.
         * @return Number of failed messages.
         */
        public int getFailed() {
            return failed;
        }

        /**
         * Set number of messages that were sent with failure.
         * @param failed Number of failed messages.
         */
        public void setFailed(int failed) {
            this.failed = failed;
        }

        /**
         * Get total number of messages that were attempted to send.
         * @return Total number of messages.
         */
        public int getTotal() {
            return total;
        }

        /**
         * Set total number of messages that were attempted to send.
         * @param total Total number of messages.
         */
        public void setTotal(int total) {
            this.total = total;
        }
    }

    public static class Android {

        private int sent;
        private int failed;
        private int total;

        /**
         * Get number of messages that were sent successfully.
         * @return Number of sent messages.
         */
        public int getSent() {
            return sent;
        }

        /**
         * Set number of messages that were sent successfully.
         * @param sent Number of sent messages.
         */
        public void setSent(int sent) {
            this.sent = sent;
        }

        /**
         * Get number of messages that were sent with failure.
         * @return Number of failed messages.
         */
        public int getFailed() {
            return failed;
        }

        /**
         * Set number of messages that were sent with failure.
         * @param failed Number of failed messages.
         */
        public void setFailed(int failed) {
            this.failed = failed;
        }

        /**
         * Get total number of messages that were attempted to send.
         * @return Total number of messages.
         */
        public int getTotal() {
            return total;
        }

        /**
         * Set total number of messages that were attempted to send.
         * @param total Total number of messages.
         */
        public void setTotal(int total) {
            this.total = total;
        }
    }

    private iOS ios;
    private Android android;

    public PushSendResult() {
        this.ios = new iOS();
        this.android = new Android();
    }

    /**
     * Data associated with push messages sent to Android devices.
     */
    public Android getAndroid() {
        return android;
    }

    /**
     * Data associated with push messages sent to iOS devices.
     */
    public iOS getIos() {
        return ios;
    }

}
