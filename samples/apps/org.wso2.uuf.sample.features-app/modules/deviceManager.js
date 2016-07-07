/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Device Manager Module.
 * @typedef {{name: string, owner: string}} Device
 */
var deviceManager = {};

(function (deviceManager) {
    /**
     * Validate this device.
     * Returns true if valid, false otherwise.
     * @param {Device} device
     */
    function validateDevice(device) {
        //private method
        return true;
    }

    /**
     * Returns devices of this user.
     * @param {string} username
     * @returns {Object.<string,Device>} devices
     */
    deviceManager.getDevices = function (username) {
        //public method
        //call osgi-service and retrieve devices code here...
        return [{name: "device1"}, {name: "device2"}, {name: "device3"}];
    };
})(deviceManager);