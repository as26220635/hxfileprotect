package cn.kim.common.tag;

import cn.kim.common.attr.*;
import cn.kim.entity.DictInfo;
import cn.kim.entity.DictType;
import cn.kim.service.FileService;
import cn.kim.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.jsp.JspException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 余庚鑫 on 2018/3/29
 * 文件上传
 */
@Setter
@Getter
public class FileInput extends BaseTagSupport {
    /**
     * 图片
     */
    public static final String IMAGE = "IAMGE";
    /**
     * 视频
     */
    public static final String VIDEO = "VIDEO";
    /**
     * 文件
     */
    public static final String FILE = "FILE";
    /**
     * 预览类型
     */
    private static final String FILE_TYPE_IMG = "image";
    private static final String FILE_TYPE_PDF = "pdf";
    private static final String FILE_TYPE_HTML = "html";
    private static final String FILE_TYPE_TEXT = "text";
    private static final String FILE_TYPE_OFFICE = "office";
    private static final String FILE_TYPE_VIDEO = "video";
    private static final String FILE_TYPE_AUDIO = "audio";
    private static final String FILE_TYPE_FLASH = "flash";
    private static final String FILE_TYPE_OBJECT = "object";
    private static final String FILE_TYPE_OTHER = "other";

    /**
     * 文件服务
     */
    private FileService fileService;

    /**
     * 标题
     */
    private String title = "附件";
    /**
     * 字典代码
     */
    private String sdtCode = "";
    /**
     * 上传表主键
     */
    private String tableId = "";
    /**
     * 上传表名
     */
    private String tableName = "";
    /**
     * 初始路径
     */
    private String typeCode = "";
    /**
     * 是否可以不用登录查看 1 是 0 否
     */
    private int seeType = Attribute.STATUS_SUCCESS;
    /**
     * 是否多选 默认多选
     */
    private boolean multiple = true;
    /**
     * 上传文件主题 默认为缩略预览  主题 : explorer
     */
    private String theme = "";
    /**
     * 是否开启异步上传
     */
    private boolean showUpload = true;
    /**
     * 是否开启删除
     */
    private boolean showRemove = true;
    /**
     * 最大上传数量
     */
    private int maxFilesNum = 99;
    /**
     * 最大允许同时上传数量
     */
    private int maxFileCount = 99;
    /**
     * 最大允许上传大小
     */
    private long maxFileSize = 8000;
    /**
     * 上传类型 默认只能上传图片 为true可以上传文件
     */
    private String allowFile = IMAGE;
    /**
     * 是否单上传模式
     */
    private String nonModel = "";

    @Override
    protected int doStartTagInternal() throws Exception {
        if (!isEmpty(tableId)) {
            tableId = toString(CommonUtil.idDecrypt(tableId));
        }

        //获取bean
        this.fileService = this.getRequestContext().getWebApplicationContext().getBean(FileService.class);

        //查询字典
        DictType dictType = DictUtil.getDictType(sdtCode);
        if (ValidateUtil.isEmpty(sdtCode)) {
            return EVAL_BODY_INCLUDE;
        }

        if (isEmpty(dictType)){
            dictType = new DictType();
            dictType.setSdtCode(sdtCode);

            List<DictInfo> infoList = new ArrayList<>();
            DictInfo d = new DictInfo();
            d.setSdiRequired(0);
            d.setSdiName("附件");
            d.setIsStatus(Attribute.STATUS_SUCCESS);
            infoList.add(d);

            dictType.setInfos(infoList);
        }


        Map<String, Object> mapParam = Maps.newHashMapWithExpectedSize(4);

        StringBuilder builder = new StringBuilder();

        //最外层的ID
        String groupId = uuid();

        builder.append("<div class='box box-solid'>" +
                "   <div class='box-header with-border'>" +
                "       <h3 class='box-title'><i class='mdi mdi-file-multiple'></i>" + title + "</h3>" +
                "   </div>" +
                "   <div class='box-body'>");
        builder.append("<div class='box-group' id='" + groupId + "'>");

        //循环插入
        FuncUtil.forEach(dictType.getInfos(), (index, info) -> {
            if (info.getIsStatus() == Attribute.STATUS_ERROR){
                return;
            }
            int required = info.getSdiRequired();
            String boxClass = required == Attribute.STATUS_SUCCESS ? " box-danger " : " box-success ";
            String aClass = (index == 0 ? "" : " collapsed ") + (required == Attribute.STATUS_SUCCESS ? " text-red " : " text-black ");
            String collapseStyle = index == 0 ? "" : "height: 0px; ";
            String collapseClass = index == 0 ? " in " : "";
            //最大上传数
            String maxCount = info.getMaxCount();

            //查询改SDI_CODE下面拥有的文件
            List<Map<String, Object>> files = Lists.newArrayList();
            if (!isEmpty(tableId)) {
                mapParam.clear();
                mapParam.put("SF_TABLE_ID", tableId);
                mapParam.put("SF_TABLE_NAME", tableName);
                mapParam.put("SF_SDT_CODE", sdtCode);
                mapParam.put("SF_SDI_CODE", info.getSdiCode());
                files = fileService.selectFileList(mapParam);
            }


            builder.append("<div class='panel box " + boxClass + "'>");

            String collapseId = uuid();
            String inputId = uuid();
            String numberId = uuid();
            //标签DIV
            builder.append("<div class='box-header with-border' data-toggle='#" + collapseId + "'><h4 class='box-title' style='width: 100%;'>");
            builder.append("<a data-toggle='collapse' data-parent='#" + groupId + "' href='#" + collapseId + "' aria-expanded='true' class='" + aClass + "'>");
            builder.append(info.getSdiName() + (required == Attribute.STATUS_SUCCESS ? "（必填）" : "") + "<span class='pull-right'>数量:<span id='" + numberId + "' class='" + (required == Attribute.STATUS_SUCCESS ? "file-validate" : "") + "' data-validate-message='" + info.getSdiName() + "'>" + files.size() + "</span></span>");
            builder.append("</a>");
            builder.append("</h4></div>");

            //内容DIV
            builder.append("<div id='" + collapseId + "' class='panel-collapse collapse " + collapseClass + "' aria-expanded='true' style='" + collapseStyle + "'>");
            builder.append("<input id='" + inputId + "' name='" + inputId + "' type='file' class='file' " + getAccept(allowFile) + (multiple ? " multiple " : "") + " >");
            //插入格式化文件上传的JS
            builder.append("<script>");

            //拿到预览需要的数组
            String[] initialPreview = new String[files.size()];
            String[] initialPreviewConfig = new String[files.size()];
            FuncUtil.forEach(files, (i, file) -> {
                String id = idEncrypt(file.get("ID"));
                String SF_NAME = toString(file.get("SF_NAME"));
                String SF_PATH = toString(file.get("SF_PATH"));

                String fileType = getFileTypeName(toString(file.get("SF_SUFFIX")));
                if (fileType.equals(FILE_TYPE_IMG)) {
                    initialPreview[i] = mosaicImg(getBaseUrl() + AttributePath.FILE_PREVIEW_URL + id, file.get("SF_ORIGINAL_NAME"));
                } else if (fileType.equals(FILE_TYPE_PDF)) {
                    initialPreview[i] = mosaicPdf(getBaseUrl() + AttributePath.FILE_PREVIEW_URL + id);
                } else if (fileType.equals(FILE_TYPE_OFFICE)) {
                    initialPreview[i] = mosaicOffice(getBaseUrl() + AttributePath.FILE_OFFICE_URL + id);
                } else if (fileType.equals(FILE_TYPE_VIDEO)) {
                    //加密视频地址
                    file.put("VIDEO_URL", SF_PATH + "@@@" + SF_NAME);
                    FileUtil.filePathTobase64(file, "VIDEO_URL");
                    initialPreview[i] = mosaicVideo(WebConfig.WEBCONFIG_FILE_SERVER_URL + Url.FILE_SERVER_PLAYER_URL + toString(file.get("VIDEO_URL")));
                } else {
                    initialPreview[i] = mosaicDefault();
                }
                //文件配置
                initialPreviewConfig[i] = "{" +
                        "caption:'" + toString(file.get("SF_ORIGINAL_NAME")) + "'," +
                        "width:'140px'," +
                        "url:FILE_DEL," +
                        "downloadUrl:FILE_DOWN + '" + id + "'," +
                        "key:'" + id + "'," +
                        "size:" + toString(file.get("SF_SIZE")) + "," +
                        "type:'" + fileType + "'," +
                        "extra:{caption:'" + toString(file.get("SF_ORIGINAL_NAME")) + "',title:'" + title + "'}," +
                        "},";
            });
            //js数组名称
            String enclosureId = "enclosure" + random();
            String enclosureNameId = "enclosureName" + random();

            builder.append("var " + enclosureId + " = " + TextUtil.toString(initialPreview) + ";");
            builder.append("var " + enclosureNameId + " = " + TextUtil.toString(initialPreviewConfig, false) + ";");
            builder.append("file.init({" +
                    "id:'#" + inputId + "'," +
                    "numberId:'#" + numberId + "'," +
                    "theme:'" + theme + "'," +
                    "uploadExtraData:{SF_TABLE_ID:'" + idEncrypt(tableId) + "',SF_TABLE_NAME:'" + idEncrypt(tableName) + "',SF_TYPE_CODE:'" + idEncrypt(typeCode) + "',SF_SEE_TYPE:'" + idEncrypt(seeType) + "',SF_SDT_CODE:'" + idEncrypt(sdtCode) + "',SF_SDI_CODE:'" + idEncrypt(info.getSdiCode()) + "'}," +
                    "showUpload:" + toString(showUpload) + "," +
                    "showRemove:" + toString(showRemove) + "," +
                    (isEmpty(nonModel) ? "" : "nonModel:" + toString(nonModel) + ",") +
                    "allowedFileExtensions:" + getAllowedFileExtensions(allowFile) + "," +
                    "maxFileSize:" + maxFileSize + "," +
                    "maxFilesNum:" + (isEmpty(maxCount) ? maxFilesNum : maxCount) + "," +
                    "maxFileCount:" + (isEmpty(maxCount) ? maxFileCount : maxCount) + "," +
                    "initialPreview:" + enclosureId + "," +
                    "initialPreviewConfig:" + enclosureNameId + "," +
                    //删除数量-1
                    "}).on('filedeleted', function() {$('#" + numberId + "').text(Number($('#" + numberId + "').text()) - 1); })" +
                    //上传数量+1
                    ".on('fileuploaded', function() {$('#" + numberId + "').text(Number($('#" + numberId + "').text()) + 1);});");

            builder.append("</script>");
            builder.append("</div>");

            builder.append("</div>");
        });


        builder.append("</div></div></div>");
        //解决多层模态框关闭导致下一层不能滚动
        builder.append("<script>");
        builder.append("$('#kvFileinputModal').on('hidden.bs.modal', function () {" +
                "        if ($('.model-custom').length > 0) {" +
                "            $(document.body).addClass('modal-open');" +
                "        }" +
                "    });");
        builder.append("</script>");
        //点击头切换事件
        builder.append("<script>");
        builder.append("$('.box-header[data-toggle]').unbind('click').on('click', function () {" +
                "          $('#" + groupId + " div.collapse').collapse('hide');" +
                "          $($(this).attr('data-toggle')).collapse('toggle');" +
                "    });");
        builder.append("</script>");

        pageContext.getOut().print(builder.toString());

        return SKIP_BODY;
    }

    /**
     * 清除参数
     *
     * @return
     * @throws JspException
     */
    @Override
    public int doEndTag() throws JspException {
        title = "附件";
        sdtCode = "";
        tableId = "";
        tableName = "";
        typeCode = "";
        seeType = Attribute.STATUS_SUCCESS;
        multiple = true;
        theme = "";
        showUpload = true;
        showRemove = true;
        maxFilesNum = 99;
        maxFileCount = 99;
        maxFileSize = 8000;
        allowFile = IMAGE;
        nonModel = "";
        return super.doEndTag();
    }

    /**
     * 可以接受参数
     *
     * @param allowFile
     * @return
     */
    public String getAccept(String allowFile) {
        if (IMAGE.equals(allowFile)) {
            return " accept='image/*' ";
        } else if (VIDEO.equals(allowFile)) {
            return " accept='video/*' ";
        }
        return " ";
    }

    /**
     * 获得接受类型
     *
     * @param allowFile
     * @return
     */
    public String getAllowedFileExtensions(String allowFile) {
        if (IMAGE.equals(allowFile)) {
            return TextUtil.toString(ConfigProperties.ALLOW_SUFFIX_IMG);
        } else if (VIDEO.equals(allowFile)) {
            return TextUtil.toString(ConfigProperties.ALLOW_SUFFIX_VIDEO);
        }
        return TextUtil.toString(ConfigProperties.ALLOW_SUFFIX_FILE);
    }


    /**
     * 获取文件类型名称
     *
     * @param fileName
     * @return
     */
    private String getFileTypeName(String fileName) {
        fileName = fileName.toLowerCase();

        if (fileName.endsWith("jpg") || fileName.endsWith("jpeg") || fileName.endsWith("png") || fileName.endsWith("gif") || fileName.endsWith("bmp") || fileName.endsWith("jpg")) {
            return FILE_TYPE_IMG;
        } else if (fileName.endsWith("pdf")) {
            return FILE_TYPE_PDF;
        } else if (fileName.endsWith("doc") || fileName.endsWith("docx") || fileName.endsWith("xls") || fileName.endsWith("xlsx") || fileName.endsWith("ppt") || fileName.endsWith("pptx")) {
            return FILE_TYPE_OFFICE;
        } else if (fileName.endsWith("mov") || fileName.endsWith("mp4")) {
            return FILE_TYPE_VIDEO;
        } else {
            return FILE_TYPE_OTHER;
        }
    }

    private String mosaicDefault() {
        return "<div class=\"kv-preview-data file-preview-other-frame\"><div class=\"file-preview-other\"><span class=\"file-other-icon\"><i class=\"glyphicon glyphicon-king\"></i></span></div></div>";
    }

    private String mosaicThumbnail(Object url) {
        return "<img src=\"" + toString(url) + "\" class=\"margin\" data-action=\"zoom\" height=\"auto\" width=\"100\" >";
    }

    private String mosaicImg(Object url, Object name) {
        return "<img src=\"" + toString(url) + "\" class=\"file-preview-image kv-preview-data\" title=\"" + name + "\" alt=\"" + name + "\" style=\"width:auto;height:150px;\">";
    }

    private String mosaicPdf(Object url) {
        return "<embed class=\"kv-preview-data file-preview-pdf\" src=\"" + toString(url) + "\" type=\"application/pdf\" style=\"width:100%;height:100%;\" />";
    }

    private String mosaicOffice(Object url) {
        return "<iframe class=\"kv-preview-data file-preview-office\" src=\"" + toString(url) + "\"></iframe>";
    }

    private String mosaicVideo(Object url) {
        return "<video class=\"kv-preview-data file-preview-video\" controls=\"\" style=\"width:213px;height:160px;\"><source src=\"" + toString(url) + "\" type=\"video/mp4\"><div class=\"file-preview-other\"><span class=\"file-other-icon\"><i class=\"glyphicon glyphicon-file\"></i></span></div></video>";
    }
}

