package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.model.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

	/**
	 * 返回Content全部列表
	 * @return
	 */
	@Override
    public List<Content> getAll(){
        return contentMapper.selectAll();
    }


    /***
     * 分页返回Content列表
     * @param pageNum
     * @param pageSize
     * @return
     */
	@Override
    public PageInfo<Content> getAll(Content content,int pageNum, int pageSize) {
        //执行分页
        PageHelper.startPage(pageNum,pageSize);
       
        //执行查询
        List<Content> all = contentMapper.select(content);
        PageInfo<Content> pageInfo = new PageInfo<Content>(all);
        return pageInfo;
    }



    /***
     * 增加Content信息
     * @param content
     * @return
     */
    @Override
    public int add(Content content) {
        int count = contentMapper.insertSelective(content);
        if (count > 0) {
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }
        return count;
    }


    /***
     * 根据ID查询Content信息
     * @param id
     * @return
     */
    @Override
    public Content getOneById(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }


    /***
     * 根据ID修改Content信息
     * @param content
     * @return
     */
    @Override
    public int updateContentById(Content content) {
        Content oldContent = contentMapper.selectByPrimaryKey(content.getId());
        int count = contentMapper.updateByPrimaryKeySelective(content);
        if (count > 0) {
            redisTemplate.boundHashOps("content").delete(oldContent.getCategoryId());
            if (content.getCategoryId().longValue() != oldContent.getCategoryId().longValue()) {
                redisTemplate.boundHashOps("content").delete(content.getCategoryId());
            }
        }
        return count;
    }


    /***
     * 根据ID批量删除Content信息
     * @param ids
     * @return
     */
    @Override
    public int deleteByIds(List<Long> ids) {
        //查询出所有分类
        Example example1 = new Example(Content.class);
        Example.Criteria criteria1 = example1.createCriteria();
        criteria1.andIn("id",ids);
        List<Content> contents = contentMapper.selectByExample(example1);

        //创建Example，来构建根据ID删除数据
        Example example = new Example(Content.class);
        Example.Criteria criteria = example.createCriteria();

        //所需的SQL语句类似 delete from tb_content where id in(1,2,5,6)
        criteria.andIn("id",ids);
        int count = contentMapper.deleteByExample(example);

        //删除对应缓存
        if (count > 0) {
            for (Content content : contents) {
                redisTemplate.boundHashOps("content").delete(content.getCategoryId());
            }
        }
        return count;
    }

    @Override
    public List<Content> findByCategoryId(long categoryId) {
        List<Content> contents = (List<Content>)redisTemplate.boundHashOps("content").get(categoryId);
        if (contents != null) {
            return contents;
        }
        Example example = new Example(Content.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("categoryId",categoryId);
        criteria.andEqualTo("status",Content.CONTENT_STATUS_START);
        example.orderBy("sortOrder").asc();
        contents = contentMapper.selectByExample(example);
        if (contents != null && contents.size() > 0) {
            //存入缓存
            redisTemplate.boundHashOps("content").put(categoryId,contents);
        }
        return contents;
    }
}
