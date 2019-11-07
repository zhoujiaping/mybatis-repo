package org.sirenia.repo.model
class IndexPage implements Serializable{
    private static final long serialVersionUID = 1L

    int total
    /** 当前页,从1开始 */
    int index
    /** 每页显示记录数 */
    int size
}
