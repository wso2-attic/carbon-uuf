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

function onRequest() {
    var listOfPets = [{id: "1", name: "Snowy", category: "Puppy", color: "White"},
        {id: "2", name: "Kitty", category: "Kitten", color: "Brown"},
        {id: "3", name: "Blacky", category: "Bunny", color: "Black"},
        {id: "4", name: "Pinky", category: "Squirrel", color: "Grey"},
        {id: "5", name: "Tutu", category: "Parrot", color: "Green"}];
    return {pets: listOfPets};
}