/*
 * Copyright 2016 Wultra s.r.o.
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
 * @author Petr Dvorak, petr@wultra.com
 */
public class PushMessageSendResult {

    /**
     * Result for the iOS platform.
     */
    public static class iOS {

        private int sent;
        private int failed;
        private int pending;
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
         * Get number of messages that are still in pending state after attempted sending.
         * @return Number of pending messages.
         */
        public int getPending() {
            return pending;
        }

        /**
         * Set number of messages that are still in pending state after attempted sending.
         * @param pending Number of pending messages.
         */
        public void setPending(int pending) {
            this.pending = pending;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof iOS)) return false;

            iOS iOS = (iOS) o;

            if (getSent() != iOS.getSent()) return false;
            if (getFailed() != iOS.getFailed()) return false;
            if (getPending() != iOS.getPending()) return false;
            return getTotal() == iOS.getTotal();
        }

        @Override
        public int hashCode() {
            int result = getTotal();
            result = 31 * result + getFailed();
            result = 31 * result + getPending();
            result = 31 * result + getSent();
            return result;
        }
    }

    /**
     * Result for the Android platform.
     */
    public static class Android {

        private int sent;
        private int failed;
        private int pending;
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
         * Get number of messages that are still in pending state after attempted sending.
         * @return Number of pending messages.
         */
        public int getPending() {
            return pending;
        }

        /**
         * Set number of messages that are still in pending state after attempted sending.
         * @param pending Number of pending messages.
         */
        public void setPending(int pending) {
            this.pending = pending;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Android)) return false;

            Android android = (Android) o;

            if (getSent() != android.getSent()) return false;
            if (getFailed() != android.getFailed()) return false;
            if (getPending() != android.getPending()) return false;
            return getTotal() == android.getTotal();
        }

        @Override
        public int hashCode() {
            int result = getTotal();
            result = 31 * result + getFailed();
            result = 31 * result + getPending();
            result = 31 * result + getSent();
            return result;
        }
    }

    private final iOS ios;
    private final Android android;

    /**
     * Default constructor.
     */
    public PushMessageSendResult() {
        this.ios = new iOS();
        this.android = new Android();
    }

    /**
     * Data associated with push messages sent to Android devices.
     * @return Data related to FCM service.
     */
    public Android getAndroid() {
        return android;
    }

    /**
     * Data associated with push messages sent to iOS devices.
     * @return Data related to APNS service.
     */
    public iOS getIos() {
        return ios;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PushMessageSendResult)) return false;

        PushMessageSendResult that = (PushMessageSendResult) o;

        if (getIos() != null ? !getIos().equals(that.getIos()) : that.getIos() != null) return false;
        return getAndroid() != null ? getAndroid().equals(that.getAndroid()) : that.getAndroid() == null;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (getAndroid() != null ? getAndroid().hashCode() : 0);
        result = 31 * result + (getIos() != null ? getIos().hashCode() : 0);
        return result;
    }
}
