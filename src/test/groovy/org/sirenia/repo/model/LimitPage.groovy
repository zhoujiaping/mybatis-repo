package org.sirenia.repo.model
/**
 * limit和offset方式的分页
 */
class LimitPage implements Serializable{

    private static final long serialVersionUID = 1L

    int limit
    int offset
    int total
}
