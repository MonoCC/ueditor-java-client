package com.baidu.ueditor.upload;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.baidu.ueditor.ConfigManager;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.State;
import com.baidu.ueditor.listener.UploadListener;

public class Uploader {
	private HttpServletRequest request = null;
	private Map<String, Object> conf = null;
	private List<UploadListener> listeners;

	public Uploader(HttpServletRequest request, Map<String, Object> conf, List<UploadListener> listeners) {
		this.request = request;
		this.conf = conf;
		this.listeners = listeners;
	}

	public final State doExec() {
		if(listeners != null && !listeners.isEmpty()) {
			for(UploadListener listener : listeners) {
				listener.beforeUpload(request);
			}
		}
		State state = null;

		if ("true".equals(this.conf.get("isBase64"))) {
			state = Base64Uploader.save(this.request, this.conf);
		} else {
			state = BinaryUploader.save(this.request, this.conf);
		}
		if(listeners != null && !listeners.isEmpty()) {
			String physicalPath = null;
			if(state.isSuccess()) {
				BaseState bs = (BaseState) state;
				String rootPath = ConfigManager.getRootPath(request,conf);
				physicalPath = rootPath + bs.getInfoMap().get("url");
			}
			for(UploadListener listener : listeners) {
				listener.afterUpload(request, state, physicalPath);
			}
		}
		return state;
	}
}
