package com.pinyougou.sellergoods.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.GoodsMapper;
import com.pinyougou.model.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

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
        List<Goods> all = goodsMapper.select(goods);
        PageInfo<Goods> pageInfo = new PageInfo<Goods>(all);
        return pageInfo;
    }



    /***
     * 增加Goods信息
     * @param goods
     * @return
     */
    @Override
    public int add(Goods goods) {
        return goodsMapper.insertSelective(goods);
    }


    /***
     * 根据ID查询Goods信息
     * @param id
     * @return
     */
    @Override
    public Goods getOneById(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }


    /***
     * 根据ID修改Goods信息
     * @param goods
     * @return
     */
    @Override
    public int updateGoodsById(Goods goods) {
        return goodsMapper.updateByPrimaryKeySelective(goods);
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
        return goodsMapper.deleteByExample(example);
    }
}
