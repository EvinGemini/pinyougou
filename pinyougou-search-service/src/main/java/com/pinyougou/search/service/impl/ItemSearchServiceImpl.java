package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.model.Item;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map search) {
        SimpleHighlightQuery query = new SimpleHighlightQuery(new SimpleStringCriteria("*:*"));
        //高亮信息设置
        highlightSetting(query);
        //设置查询的条件
        if (search != null) {
            //关键词过滤
            String keyword = search.get("keyword").toString();
            if (StringUtils.isNotBlank(keyword)) {
                Criteria criteria = new Criteria("item_keywords").is(keyword);
                query.addCriteria(criteria);
            }

            //分类过滤
            String category = search.get("category").toString();
            if (StringUtils.isNotBlank(category)) {
                //创建过滤条件
                Criteria criteria = new Criteria("item_category").is(category);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }

            //品牌过滤
            String brand = search.get("brand").toString();
            if (StringUtils.isNotBlank(brand)) {
                //创建过滤条件
                Criteria criteria = new Criteria("item_brand").is(brand);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }

            //规格过滤
            Object spec = search.get("spec");
            if (spec != null) {
                Map<String, String> specMap = JSON.parseObject(spec.toString(), Map.class);
                for (Map.Entry<String, String> entry : specMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    //创建过滤条件
                    Criteria criteria = new Criteria("item_spec_" + key).is(value);
                    FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                    query.addFilterQuery(filterQuery);
                }
            }
        }
        //高亮信息匹配
        HighlightPage<Item> highlightPage = highlightMatch(query);
        //查询分类信息
        List<String> categoryList = searchCategory(query);      //分类信息
        Map<String, Object> dataMap = new HashMap<>();

        //每次点击不同分类时，品牌和规格应该跟着一起变动。
        String category = search.get("category").toString();
        if (StringUtils.isNotBlank(category)) {
            getBrandAndSpec(category, dataMap);
        } else {
            if (categoryList != null && categoryList.size() > 0) {
                getBrandAndSpec(categoryList.get(0), dataMap);
            }
        }
        //得到的是替换标题后的非高亮数据
        List<Item> items = highlightPage.getContent();
        dataMap.put("rows", items);
        dataMap.put("total", highlightPage.getTotalElements());
        //添加分类信息
        dataMap.put("categoryList", categoryList);

        return dataMap;
    }

    //获取品牌和规格
    private void getBrandAndSpec(String category, Map<String, Object> dataMap) {
        //根据分类获取模板id
        Long typeTemplateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeTemplateId != null) {
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("BrandList").get(typeTemplateId);
            dataMap.put("brandList", brandList);
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("SpecList").get(typeTemplateId);
            dataMap.put("specList", specList);
        }

    }

    /****
     * 查询所有分类数据
     * @param query
     * @return
     */
    public List<String> searchCategory(Query query) {
        //设置分组信息
        GroupOptions groupOptions = new GroupOptions();
        //设置分组域
        groupOptions.addGroupByField("item_category");
        //设置分组选项
        query.setGroupOptions(groupOptions);
        //执行查询
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);
        //获取对应域的分组信息
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");
        //循环获得分组数据
        List<String> list = new ArrayList<>();
        for (GroupEntry<Item> groupEntry : groupResult.getGroupEntries()) {
            list.add(groupEntry.getGroupValue());
        }
        return list;
    }


    /**
     * 高亮信息匹配
     *
     * @param query
     * @return
     */
    private HighlightPage<Item> highlightMatch(SimpleHighlightQuery query) {
        //查询
        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(query, Item.class);
        //获取高亮数据和非高亮集合
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        //循环的是非高亮数据的大小
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //获取非高亮数据
            Item item = itemHighlightEntry.getEntity();
            //获取高亮数据集合
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
            if (highlights != null && highlights.size() > 0) {
                //获取高亮记录
                HighlightEntry.Highlight highlight = highlights.get(0);
                //从高亮记录中获取高亮数据
                List<String> snipplets = highlight.getSnipplets();
                //判断数据是否为空
                if (snipplets != null && snipplets.size() > 0) {
                    item.setTitle(snipplets.get(0));
                }
            }
        }
        return highlightPage;
    }

    /**
     * 高亮信息设置
     *
     * @param query
     */
    private void highlightSetting(SimpleHighlightQuery query) {
        //创建高亮选项
        HighlightOptions highlightOptions = new HighlightOptions();
        //添加高亮域
        highlightOptions.addField("item_title");
        //前缀
        highlightOptions.setSimplePrefix("<span style=\"color:red;\">");
        //后缀
        highlightOptions.setSimplePostfix("</span>");
        //添加高亮配置
        query.setHighlightOptions(highlightOptions);
    }
}
