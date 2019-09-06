package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.model.Item;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map search) {
        Query query = new SimpleQuery("*:*");
        if (search != null) {
            String keyword = search.get("keyword").toString();
            if(StringUtils.isNotBlank(keyword)){
                Criteria criteria = new Criteria("item_keywords").is(keyword);
                query.addCriteria(criteria);
            }


        }
        ScoredPage<Item> scoredPage = solrTemplate.queryForPage(query, Item.class);
        Map<String, Object> dataMap = new HashMap<>();
        List<Item> items = scoredPage.getContent();
        dataMap.put("rows",items);
        dataMap.put("total",scoredPage.getTotalElements());
        return dataMap;
    }
}
