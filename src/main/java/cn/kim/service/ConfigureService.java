package cn.kim.service;

import cn.kim.service.impl.BaseServiceImpl;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Created by 余庚鑫 on 2018/3/26
 */
public interface ConfigureService extends BaseService {
    Map<String, Object> selectConfigure(Map<String, Object> mapParam);

    List<Map<String, Object>> selectConfigureList(Map<String, Object> mapParam);

    List<Map<String, Object>> selectConfigureColumnList(Map<String, Object> mapParam);

    List<Map<String, Object>> selectConfigureSearchList(Map<String, Object> mapParam);

    Map<String, Object> insertAndUpdateConfigure(Map<String, Object> mapParam);

    /**
     * 复制配置列表
     * @param mapParam
     * @return
     */
    Map<String, Object> copyConfigure(Map<String, Object> mapParam);

    Map<String, Object> deleteConfigure(Map<String, Object> mapParam);

    /********   字段  *******/
    Map<String, Object> selectConfigureColumn(Map<String, Object> mapParam);

    Map<String, Object> insertAndUpdateConfigureColumn(Map<String, Object> mapParam);

    Map<String, Object> deleteConfigureColumn(Map<String, Object> mapParam);

    /********   搜索  *******/
    Map<String, Object> selectConfigureSearch(Map<String, Object> mapParam);

    Map<String, Object> insertAndUpdateConfigureSearch(Map<String, Object> mapParam);

    Map<String, Object> deleteConfigureSearch(Map<String, Object> mapParam);

    /********   文件  *******/
    Map<String, Object> selectConfigureFile(Map<String, Object> mapParam);

    Map<String, Object> insertAndUpdateConfigureFile(Map<String, Object> mapParam, MultipartFile file);

    Map<String, Object> changeConfigureFileStatus(Map<String, Object> mapParam);

    Map<String, Object> deleteConfigureFile(Map<String, Object> mapParam);
}
