package org.sirenia.repo.model

class PageRes<T extends Serializable> implements Serializable{
    private static final long serialVersionUID = 1L

    Integer total
    List<T> rows
}