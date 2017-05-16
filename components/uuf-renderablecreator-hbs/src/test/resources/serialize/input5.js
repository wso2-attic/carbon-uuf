/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var input = {
    url: [
        "http://www.example.com/",
        "https://www.example.com/",
        "http://www.example.com?param1=a&param2=b",
        "https://localhost:9443/carbon/console"
    ],
    uri: [
        "/a/b/c",
        "/a/b/c?param1=A&param2=B",
        "jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=abc"
    ],
    script: [
        "<script>alert('some message');</script>",
        "onClick=\"showMessage();\""
    ],
    html: [
        "<div>some text</div>",
        "<a href=\"some/uri\">Click</a>"
    ],
    query: [
        "SET NOCOUNT ON;select test='test';",
        "SELECT * FROM table1 INNER JOIN table2 ON table1.column_name=table2.column_name;",
        "select * from Employee where Rowid= select max(Rowid) from Employee",
        "Select * from Employee a where row_id!=select max(row_id) for Employee b where a.Employee_num=b.Employee_num;"
    ]
};
