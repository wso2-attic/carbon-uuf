/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.sample.featuresapp.bundle;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents a collection of {@link Book} objects.
 */
public class Library {

    private List<Book> books = Arrays.asList(new Book(1234, "Harry Potter and the Deathly Hallows", "J. K. Rowling"));

    /**
     * Returns the Book object corresponding to the id passed.
     *
     * @param id the @link {@link Book#id} of the required book.
     * @return the {@link Book} object corresponding to the id or null if the book isn't found.
     */
    public Book getBook(int id) {
        Optional<Book> book = books.stream()
                .filter(b -> b.getId() == id)
                .findFirst();
        return book.isPresent() ? book.get() : null;
    }
}
