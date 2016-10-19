package com.baidu.ueditor.upload;

import com.baidu.ueditor.ConfigManager;
import com.baidu.ueditor.PathFormat;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.FileType;
import com.baidu.ueditor.define.State;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

public class BinaryUploader {
	

	public static final State save(HttpServletRequest request, Map<String, Object> conf) {
		
		if (!ServletFileUpload.isMultipartContent(request)) {
			return new BaseState(false, AppInfo.NOT_MULTIPART_CONTENT);
		}

		DefaultMultipartHttpServletRequest mRequest = (DefaultMultipartHttpServletRequest) request;
		

		Map<String, MultipartFile> map = mRequest.getMultiFileMap().toSingleValueMap();
		if(map.isEmpty()) {
			return new BaseState(false, AppInfo.NOTFOUND_UPLOAD_DATA);
		}
		InputStream is = null;
		String savePath = (String) conf.get("savePath");
		try {
	
			for(Map.Entry<String, MultipartFile> entry : map.entrySet()) {
				
//				String inputFileName = entry.getKey();
				MultipartFile file = entry.getValue();
				String originFileName = file.getOriginalFilename();
				String suffix = FileType.getSuffixByFilename(originFileName);

				originFileName = originFileName.substring(0,
						originFileName.length() - suffix.length());
				savePath = savePath + suffix;

				long maxSize = ((Long) conf.get("maxSize")).longValue();

				if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
					return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
				}

				savePath = PathFormat.parse(savePath, originFileName);

				//modified by Ternence
	            String rootPath = ConfigManager.getRootPath(request,conf);
	            String physicalPath = rootPath + savePath;
	            
	            
				is = file.getInputStream();
				State storageState = StorageManager.saveFileByInputStream(is, physicalPath, maxSize);
				
				if (storageState.isSuccess()) {
					storageState.putInfo("url", PathFormat.format(savePath));
					storageState.putInfo("type", suffix);
					storageState.putInfo("original", originFileName + suffix);
				}

				return storageState;
			}
//		} catch (FileUploadException e) {
//			return new BaseState(false, AppInfo.PARSE_REQUEST_ERROR);
		} catch (IOException e) {
		} finally {
			IOUtils.closeQuietly(is);
		}
		return new BaseState(false, AppInfo.IO_ERROR);
	}

	private static boolean validType(String type, String[] allowTypes) {
		List<String> list = Arrays.asList(allowTypes);

		return list.contains(type);
	}
}
