package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void batchInsert(){
        //查询所有商品
        Item iteminfo=new Item();
        //商品状态为1，表示审核通过的商品
        iteminfo.setStatus(Item.ITEM_STATUE_NORMAL);
        List<Item> items = itemMapper.select(iteminfo);

        //添加动态域
        for (Item item : items) {
            //获取规格数据
            String spec = item.getSpec();
            Map<String,String> map = JSON.parseObject(spec, Map.class);

            item.setSpecMap(map);
        }


        //批量增加商品到索引库
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }
}
