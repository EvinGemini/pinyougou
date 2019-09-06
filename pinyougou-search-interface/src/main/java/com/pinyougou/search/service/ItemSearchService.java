package com.pinyougou.search.service;

import java.util.Map;

public interface ItemSearchService {
    /***
     * 搜索
     * @param search
     * @return
     */
    Map<String,Object> search(Map search);
}
