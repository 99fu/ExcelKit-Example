package org.wuwz.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.wuwz.poi.ExcelKit;

import com.google.common.collect.Lists;

public class GenLocalExample {

	public static void main(String[] args) throws FileNotFoundException {
		
		
		List<User> users = Lists.newArrayList();
		for (int i = 0; i < 300; i++) {
			User e = new User();
			e.setUid(i + 1);
			e.setUsername("USERNAME_" + (i + 1));
			e.setPassword("asdasdasdasd");
			e.setNickname("NICKNAME_" + (i + 1));
			e.setAge(18);
			users.add(e);
		}
		
		// ���ɱ����ļ�
		File excelFile = new File("C:\\Users\\Administrator\\Desktop\\excel.xlsx");
		ExcelKit.$Builder(User.class).toExcel(users, "�û���Ϣ", new FileOutputStream(excelFile));
		
		// ����Excel����ģ���ļ���
		users.clear();
		File templateFile = new File("C:\\Users\\Administrator\\Desktop\\import_template.xlsx");
		ExcelKit.$Builder(User.class).toExcel(users, "�û���Ϣ", new FileOutputStream(templateFile));
	}
}
