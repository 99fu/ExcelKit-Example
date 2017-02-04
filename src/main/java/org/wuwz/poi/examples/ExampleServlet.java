/**

 * Copyright (c) 2017, ������ (wuwz@live.com).

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *      http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.wuwz.poi.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.wuwz.poi.ExcelKit;
import org.wuwz.poi.ExcelType;
import org.wuwz.poi.OnReadDataHandler;

/**
 * �������������
 * 
 * @author wuwz
 */
// @WebServlet("/example")
public class ExampleServlet extends HttpServlet {

	private static final long serialVersionUID = -8791212010764446339L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String t = request.getParameter("t");
		if ("list".equals(t)) {
			// ��ת���б�ҳ
			toListPage(request, response);
			
		} else if ("export".equals(t)) {
			
			// ִ��Excel�ļ�����
			ExcelKit.$Export(User.class, response).toExcel(Db.getUsers(), "�û���Ϣ");
		} else if ("downtmpl".equals(t)) {

			// ����ģ���ļ�
			downTemplFile(response);
		} else if ("import".equals(t)) {
			
			// Excel�ļ�����
			importExcelFile(request, response);
		}
	}

	private void importExcelFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		if (!ServletFileUpload.isMultipartContent(request)) {
		    writer.println("Error: ��������� enctype=multipart/form-data");
		    writer.flush();
		    return;
		}
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// �����ڴ��ٽ�ֵ - �����󽫲�����ʱ�ļ����洢����ʱĿ¼��
		factory.setSizeThreshold(1024 * 1024 * 3);
		// ������ʱ�洢Ŀ¼
		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
 
		ServletFileUpload upload = new ServletFileUpload(factory);
		// ��������ļ��ϴ�ֵ
		upload.setFileSizeMax(1024 * 1024 * 40);
		// �����������ֵ (�����ļ��ͱ�����)
		upload.setSizeMax(1024 * 1024 * 50);
 
		// ������ʱ·�����洢�ϴ����ļ�
		String uploadPath = request.getServletContext().getRealPath("./") + File.separator + "upload";
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
		    uploadDir.mkdir();
		}
 
		try {
		    // ���������������ȡ�ļ�����
		    List<FileItem> formItems = upload.parseRequest(request);
 
		    if (formItems != null && formItems.size() > 0) {
		        for (FileItem item : formItems) {
		            if (!item.isFormField()) {
		                String fileName = new File(item.getName()).getName();
		                String filePath = uploadPath + File.separator + fileName;
		                File storeFile = new File(filePath);
		                System.out.println(filePath);
		                item.write(storeFile);
		                
		                //=============��ʼ����Excel�ļ������=================
		                ExcelKit.$Import().readExcel(storeFile, new OnReadDataHandler() {
							
							@Override
							public void handler(List<String> rowData) {
								User user = new User();
								user.setUid(Integer.parseInt(rowData.get(0)));
								user.setUsername(rowData.get(1));
								user.setPassword(rowData.get(2));
								user.setNickname(rowData.get(3));
								user.setAge(18);
								Db.addUser(user);
							}
						});
		                
		                if(storeFile.exists()) {
		                	storeFile.delete();
		                }
		                // ��ת�б�ҳ
		                toListPage(request, response);
		            }
		        }
		    }
		} catch (Exception ex) {
			writer.println("Error: "+ex.getMessage());
			writer.flush();
		}
	}

	private void downTemplFile(HttpServletResponse response) throws FileNotFoundException, IOException {
		File tmplFile = new File(String.format("%s/import_template.xlsx", System.getProperty("java.io.tmpdir")));
		
		if(!tmplFile.exists()) {
			// ����ģ���ļ�
			ExcelKit.$Builder(User.class).toExcel(null, "�û���Ϣ", new FileOutputStream(tmplFile));
		}

		// ִ������
		@SuppressWarnings("deprecation")
		String fileName = URLEncoder.encode(tmplFile.getName());
		response.reset();
		response.setContentType(ExcelKit.$Import().getContentType(ExcelType.EXCEL2007));
		response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		int fileLength = (int) tmplFile.length();
		response.setContentLength(fileLength);
		if (fileLength != 0) {
			InputStream inStream = new FileInputStream(tmplFile);
			byte[] buf = new byte[4096];
			ServletOutputStream servletOS = response.getOutputStream();
			int readLength;
			while (((readLength = inStream.read(buf)) != -1)) {
				servletOS.write(buf, 0, readLength);
			}
			inStream.close();
			servletOS.flush();
			servletOS.close();
		}
	}

	private void toListPage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("users", Db.getUsers());
		request.getRequestDispatcher("/list.jsp").forward(request, response);
	}

}
