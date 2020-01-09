package cn.kim.controller.manager;

import cn.kim.common.annotation.NotEmptyLogin;
import cn.kim.common.annotation.SystemControllerLog;
import cn.kim.common.eu.UseType;
import cn.kim.exception.CustomException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 余庚鑫 on 2019/12/13
 * 图形报表
 */
@Controller
@RequestMapping("/admin/Echarts")
public class EchartsController extends BaseController {
    /**
     * 柱形
     */
    private static final String ECHARTS_BAR = "bar";
    /**
     * 饼图
     */
    private static final String ECHARTS_PIE = "pie";
    /**
     * 雷达
     */
    private static final String ECHARTS_RADAR = "radar";


    /**
     * 柱形图
     *
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/bar")
    @SystemControllerLog(useType = UseType.SEE, isEcharts = true, event = "查看柱形图")
    @NotEmptyLogin
    public String bar(@RequestParam Map<String, Object> mapParam, Model model) throws Exception {
        String v = toString(mapParam.get("v"));
        if (isEmpty(v)) {
            throw new CustomException("参数错误");
        }
        String title = "统计数据";
        String subtext = getDate();
        //动作
        String action = toString(mapParam.get("action"));

        model.addAttribute("v", v);
        model.addAttribute("title", title);
        model.addAttribute("subtext", subtext);
        model.addAttribute("chartId", UUID.randomUUID().toString());
        return "admin/system/echarts/bar";
    }

    /**
     * 饼图
     *
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/pie")
    @SystemControllerLog(useType = UseType.SEE, isEcharts = true, event = "查看饼图")
    @NotEmptyLogin
    public String pie(@RequestParam Map<String, Object> mapParam, Model model) throws Exception {
        String v = toString(mapParam.get("v"));
        if (isEmpty(v)) {
            throw new CustomException("参数错误");
        }
        String title = "统计数据";
        String subtext = getDate();
        //动作
        String action = toString(mapParam.get("action"));

        //获得数据

        model.addAttribute("v", v);
        model.addAttribute("title", title);
        model.addAttribute("subtext", subtext);
        model.addAttribute("chartId", UUID.randomUUID().toString());
        return "admin/system/echarts/pie";
    }

    /**
     * list转为bar可以使用的参数
     *
     * @param list            数据
     * @param xAxisField      X轴字段
     * @param legendField     组字段
     * @param valueFields     参数字段
     * @param valueNameFields 参数名称字段
     * @return
     */
    public Map<String, Object> toBarGroupData(List<Map<String, Object>> list, String xAxisField, String legendField, String[] valueFields, String[] valueNameFields) {
        Map<String, Object> resultMap = new HashMap<>();
        if (list == null || list.size() == 0) {
            return resultMap;
        }
        //X轴参数
        List<String> xAxisList = xAxisListSort(list, xAxisField);
        //统计组参数
        Set<String> legendSet = new LinkedHashSet<>();
        Map<String, Double> valueMap = new HashMap<>();
        list.forEach(map -> {
            for (int i = 0; i < valueFields.length; i++) {
                String valueField = valueFields[i];
                String valueNameField = valueNameFields[i];
                String legend = toString(map.get(legendField)) + valueNameField;
                Double val = toBigDecimal(map.get(valueField)).doubleValue();
                valueMap.put(toString(map.get(xAxisField)) + legend, val == null ? 0d : val);
                legendSet.add(legend);
            }
        });
        //补充每个组缺少的参数
        legendSet.forEach(legend -> {
            xAxisList.forEach(xAxis -> {
                for (String valueNameField : valueNameFields) {
                    if (valueMap.get(xAxis + legend + valueNameField) == null) {
                        valueMap.put(xAxis + legend + valueNameField, 0d);
                    }
                }
            });
        });
        //转为参数集合
        List<Map<String, Object>> seriesList = new LinkedList<>();
        legendSet.forEach(legend -> {
            Map<String, Object> series = new HashMap<>();
            //组名
            series.put("name", legend);
            //参数
            series.put("data", doubleArrayToValue(xAxisList, valueMap, legend));

            seriesList.add(series);
        });

        resultMap.put("legendArray", joinArray(legendSet.stream()));
        resultMap.put("xAxisArray", joinArray(xAxisList.stream()));
        resultMap.put("seriesList", seriesList);

        return resultMap;
    }

    /**
     * list转为bar可以使用的参数
     *
     * @param list         数据
     * @param xAxisField   X轴字段
     * @param valueFields  参数字段
     * @param legendFields 转换参数名称字段
     * @return
     */
    public Map<String, Object> toBarGroupSplitData(List<Map<String, Object>> list, String xAxisField, String[] valueFields, String[] legendFields) {
        Map<String, Object> resultMap = new HashMap<>();
        //X轴参数
        List<String> xAxisList = xAxisListSort(list, xAxisField);
        //统计组参数
        Map<String, Double> valueMap = new HashMap<>();
        list.forEach(map -> {
            for (String valueField : valueFields) {
                Double val = toBigDecimal(map.get(valueField)).doubleValue();
                valueMap.put(toString(map.get(xAxisField)) + valueField, val == null ? 0d : val);
            }
        });
        //补充每个组缺少的参数
        for (String valueField : valueFields) {
            xAxisList.forEach(xAxis -> {
                if (valueMap.get(xAxis + valueField) == null) {
                    valueMap.put(xAxis + valueField, 0d);
                }
            });
        }
        //转为参数集合
        List<Map<String, Object>> seriesList = new LinkedList<>();
        for (int i = 0; i < valueFields.length; i++) {
            String valueField = valueFields[i];
            Map<String, Object> series = new HashMap<>();
            //组名
            series.put("name", legendFields[i]);
            //参数
            series.put("data", doubleArrayToValue(xAxisList, valueMap, valueField));

            seriesList.add(series);
        }

        resultMap.put("legendArray", joinArray(Arrays.stream(legendFields)));
        resultMap.put("xAxisArray", joinArray(xAxisList.stream()));
        resultMap.put("seriesList", seriesList);

        return resultMap;
    }

    /**
     * 转为饼图数据
     *
     * @param name
     * @param value
     * @return
     */
    public String toPieArray(String[] name, int[] value) {
        if (name.length != value.length) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length; i++) {
            builder.append("{");
            builder.append("value:" + value[i] + ',');
            builder.append("name:'" + name[i] + "'");
            builder.append("},");
        }
        return builder.toString();
    }

    /**
     * 排序
     *
     * @param list
     * @param xAxisField
     * @return
     */
    public List<String> xAxisListSort(List<Map<String, Object>> list, String xAxisField) {
        Set<String> xAxisSet = new HashSet<>();
        List<String> xAxisList = new LinkedList<>();
        list.forEach(map -> {
            xAxisSet.add(toString(map.get(xAxisField)));
        });
        xAxisSet.forEach(s -> {
            xAxisList.add(s);
        });
        //排序坐标
        Collections.sort(xAxisList);
        return xAxisList;
    }

    public String joinArray(Stream<?> steam) {
        return joinArray(steam, ",");
    }

    public String joinArray(Stream<?> steam, String delimiter) {
        return steam.map(i -> "'" + i + "'").collect(Collectors.joining(delimiter));
    }

    public String joinValueArray(Stream<?> steam) {
        return steam.map(i -> toString(i)).collect(Collectors.joining(","));
    }

    public String doubleArrayToValue(List<?> xAxisList, Map<String, Double> valueMap, String valueField) {
        Double[] valueArray = xAxisList.stream().map(xAxis -> valueMap.get(xAxis + valueField)).toArray(Double[]::new);
        return joinValueArray(Arrays.stream(valueArray));
    }
}
