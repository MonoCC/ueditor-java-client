package com.baidu.ueditor.listener;

import javax.servlet.http.HttpServletRequest;

import com.baidu.ueditor.define.State;

/**
 * 上传文件监听器
 */
public interface UploadListener {
	
	/**
	 * 上传前
	 * @param request
	 */
	void beforeUpload(HttpServletRequest request);
	
	/**
	 * 上传完成后
	 * @param request
	 */
	void afterUpload(HttpServletRequest request, State state, String physicalPath);
	
	

}
