package com.pinyougou.sellergoods.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.model.*;
import com.pinyougou.sellergoods.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsDescMapper goodsDescMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SellerMapper sellerMapper;
    @Autowired
    private ItemMapper itemMapper;

	/**
	 * 返回Goods全部列表
	 * @return
	 */
	@Override
    public List<Goods> getAll(){
        return goodsMapper.selectAll();
    }


    /***
     * 分页返回Goods列表
     * @param pageNum
     * @param pageSize
     * @return
     */
	@Override
    public PageInfo<Goods> getAll(Goods goods,int pageNum, int pageSize) {
        //执行分页
        PageHelper.startPage(pageNum,pageSize);
       
        //执行查询
        Example example = new Example(Goods.class);
        Example.Criteria criteria = example.createCriteria();
        if (goods != null) {
            if (StringUtils.isNotBlank(goods.getSellerId())) {
                criteria.andEqualTo("sellerId",goods.getSellerId());
            }
            if (StringUtils.isNotBlank(goods.getAuditStatus())) {
                criteria.andEqualTo("auditStatus",goods.getAuditStatus());
            }
            if (StringUtils.isNotBlank(goods.getGoodsName())) {
                criteria.andLike("goodsName","%"+goods.getGoodsName()+"%");
            }
        }
        criteria.andIsNull("isDelete");
        List<Goods> goodsList = goodsMapper.selectByExample(example);
        PageInfo<Goods> pageInfo = new PageInfo<Goods>(goodsList);
        return pageInfo;
    }



    /***
     * 增加Goods信息
     * @param goods
     * @return
     */
    @Override
    public int add(Goods goods) {
        goods.setAuditStatus(Goods.GOODS_UNCHECKED);
        int count = goodsMapper.insertSelective(goods);
        GoodsDesc goodsDesc = goods.getGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());
        goodsDescMapper.insertSelective(goodsDesc);

        addItems(goods);
        return count;
    }

    private void addItems(Goods goods) {
        Date date = new Date();
        //分类信息
        ItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
        //品牌信息
        Brand brand = brandMapper.selectByPrimaryKey(goods.getBrandId());
        //商家信息
        Seller seller = sellerMapper.selectByPrimaryKey(goods.getSellerId());

        //添加SKU
        if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().equals("1") && goods.getItems()!=null && goods.getItems().size()>0) {
            //添加SUK信息
            for (Item item : goods.getItems()) {
                //拼接标题
                String title = goods.getGoodsName();
                Map<String, String> map = (Map<String, String>) JSON.parse(item.getSpec());
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    title += "  " + entry.getValue();
                }
                setItemInfo(goods, date, itemCat, brand, seller, item, title);
                //增加商品SKU入库
                itemMapper.insertSelective(item);
            }
        }else {
            //不启用规格
            Item item = new Item();
            //设置商品信息
            setItemInfo(goods, date, itemCat, brand, seller, item, goods.getGoodsName());
            //设置基本信息
            //价格
            item.setPrice(goods.getPrice());
            //状态
            item.setStatus(Item.ITEM_STATUE_NORMAL);
            //是否默认
            item.setIsDefault("1");
            //库存数量
            item.setNum(9999);
            //规格
            item.setSpec("{}");
            //增加商品SKU入库
            itemMapper.insertSelective(item);
        }
    }

    private void setItemInfo(Goods goods, Date date, ItemCat itemCat, Brand brand, Seller seller, Item item, String title) {
        //1设置标题
        item.setTitle(title);
        List<Map> imageMap = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        //2设置默认图片
        if (imageMap.size() > 0) {
            item.setImage(imageMap.get(0).get("url").toString());
        }
        //3商品分类编号：三级编号
        item.setCategoryid(goods.getCategory3Id());
        //4创建时间、更新时间
        item.setCreateTime(date);
        item.setUpdateTime(date);
        //5商品id
        item.setGoodsId(goods.getId());
        //6seller_id商家id
        item.setSellerId(goods.getSellerId());
        //7category
        item.setCategory(itemCat.getName());
        //8brand
        item.setBrand(brand.getName());
        //9seller
        item.setSeller(seller.getName());
    }


    /***
     * 根据ID查询Goods信息
     * @param id
     * @return
     */
    @Override
    public Goods getOneById(Long id) {
        Goods goods = goodsMapper.selectByPrimaryKey(id);
        GoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(goodsDesc);
        //获取items
        Item item = new Item();
        item.setGoodsId(goods.getId());
        List<Item> items = itemMapper.select(item);
        goods.setItems(items);

        return goods;
    }


    /***
     * 根据ID修改Goods信息
     * @param goods
     * @return
     */
    @Override
    public int updateGoodsById(Goods goods) {
        //修改状态为未审核
        goods.setAuditStatus(Goods.GOODS_UNCHECKED);
        //修改商品
        int count = goodsMapper.updateByPrimaryKeySelective(goods);
        //修改goodsDesc
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());
        //删除goodsItems
        Item item = new Item();
        item.setGoodsId(goods.getId());
        itemMapper.delete(item);
        //添加items
        addItems(goods);

        return count;
    }


    /***
     * 根据ID批量删除Goods信息
     * @param ids
     * @return
     */
    @Override
    public int deleteByIds(List<Long> ids) {
        //创建Example，来构建根据ID删除数据
        Example example = new Example(Goods.class);
        Example.Criteria criteria = example.createCriteria();

        //所需的SQL语句类似 delete from tb_goods where id in(1,2,5,6)
        criteria.andIn("id",ids);
        Goods goods = new Goods();
        goods.setIsDelete("1");
        return goodsMapper.updateByExampleSelective(goods,example);
    }

    @Override
    public int updateStatus(List<Long> ids, String status) {
        Example example = new Example(Goods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",ids);
        Goods goods = new Goods();
        goods.setAuditStatus(status);
        return goodsMapper.updateByExampleSelective(goods,example);
    }
}
