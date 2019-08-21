package com.pinyougou.sellergoods.service;
import com.github.pagehelper.PageInfo;
import com.pinyougou.model.TypeTemplate;
import java.util.List;
import java.util.Map;

public interface TypeTemplateService {

	/**
	 * 返回TypeTemplate全部列表
	 * @return
	 */
	public List<TypeTemplate> getAll();

    /***
     * 分页返回TypeTemplate列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<TypeTemplate> getAll(TypeTemplate typeTemplate, int pageNum, int pageSize);

    /***
     * 增加TypeTemplate信息
     * @param typeTemplate
     * @return
     */
    int add(TypeTemplate typeTemplate);

    /***
     * 根据ID查询TypeTemplate信息
     * @param id
     * @return
     */
    TypeTemplate getOneById(Long id);

    /***
     * 根据ID修改TypeTemplate信息
     * @param typeTemplate
     * @return
     */
    int updateTypeTemplateById(TypeTemplate typeTemplate);


    /***
     * 根据ID批量删除TypeTemplate信息
     * @param ids
     * @return
     */
    int deleteByIds(List<Long> ids);

    /**
     * 根据id查询规格选项
     * 构建数据格式
     *  *   [
     *  *       {"id":1,"text":"内存大小","options":[{"id":123,"optionName":"4G"}]},
     *  *       {"id":2,"text":"网络制式","options":[{"id":123,"optionName":"移动4G"}]},
     *  *       {"id":3,"text":"屏幕尺寸","options":[{"id":123,"optionName":"16寸"},{"id":124,"optionName":"60寸"}]}
     *  *   ];
     * @param id
     * @return
     */
    List<Map> getSpecificationOptionById(Long id);
}
