package com.pinyougou.sellergoods.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.model.SpecificationOption;
import com.pinyougou.model.TypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;
import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;
    @Autowired
    private RedisTemplate redisTemplate;

	/**
	 * 返回TypeTemplate全部列表
	 * @return
	 */
	@Override
    public List<TypeTemplate> getAll(){
        return typeTemplateMapper.selectAll();
    }


    /***
     * 分页返回TypeTemplate列表
     * @param pageNum
     * @param pageSize
     * @return
     */
	@Override
    public PageInfo<TypeTemplate> getAll(TypeTemplate typeTemplate,int pageNum, int pageSize) {
        //执行分页
        PageHelper.startPage(pageNum,pageSize);
       
        //执行查询
        List<TypeTemplate> all = typeTemplateMapper.select(typeTemplate);
        PageInfo<TypeTemplate> pageInfo = new PageInfo<TypeTemplate>(all);

        //此处调用刷新Redis缓存,缓存品牌和规格信息到Redis中
        refreshRedis();
        return pageInfo;
    }



    /***
     * 增加TypeTemplate信息
     * @param typeTemplate
     * @return
     */
    @Override
    public int add(TypeTemplate typeTemplate) {
        return typeTemplateMapper.insertSelective(typeTemplate);
    }


    /***
     * 根据ID查询TypeTemplate信息
     * @param id
     * @return
     */
    @Override
    public TypeTemplate getOneById(Long id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }


    /***
     * 根据ID修改TypeTemplate信息
     * @param typeTemplate
     * @return
     */
    @Override
    public int updateTypeTemplateById(TypeTemplate typeTemplate) {
        return typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
    }


    /***
     * 根据ID批量删除TypeTemplate信息
     * @param ids
     * @return
     */
    @Override
    public int deleteByIds(List<Long> ids) {
        //创建Example，来构建根据ID删除数据
        Example example = new Example(TypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();

        //所需的SQL语句类似 delete from tb_typeTemplate where id in(1,2,5,6)
        criteria.andIn("id",ids);
        return typeTemplateMapper.deleteByExample(example);
    }

    @Override
    public List<Map> getSpecificationOptionById(Long id) {
        //根据模板id查询出对应规格列表
        TypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        List<Map> dataMap = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
        for(Map map : dataMap) {
            SpecificationOption specificationOption = new SpecificationOption();
            specificationOption.setSpecId(Long.parseLong(map.get("id").toString()));
            List<SpecificationOption> specificationOptionList = specificationOptionMapper.select(specificationOption);
            map.put("options",specificationOptionList);
        }
        return dataMap;
    }

    /**
     * 将品牌信息和规格信息存入Redis缓存中
     */
    public void refreshRedis() {
        //获取所有模板
        List<TypeTemplate> typeTemplates = typeTemplateMapper.selectAll();
        //循环模板信息
        for (TypeTemplate typeTemplate : typeTemplates) {
            String brandIds = typeTemplate.getBrandIds();
            List<Map> brandList = JSON.parseArray(brandIds, Map.class);
            //品牌存入Redis
            redisTemplate.boundHashOps("BrandList").put(typeTemplate.getId(),brandList);
            //规格存入Redis
            List<Map> specList = getSpecificationOptionById(typeTemplate.getId());
            redisTemplate.boundHashOps("SpecList").put(typeTemplate.getId(),specList);
        }

    }
}
