package com.corebao.ssm;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.corebao.mapper.RoleMapper;
import com.corebao.pojo.Role;
import com.corebao.utils.SqlSessionFactoryUtils;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		System.out.println("Hello Max!");
		Logger log = Logger.getLogger(App.class);
		log.info("aaaa");

		SqlSession sqlSession = SqlSessionFactoryUtils.openSqlSession();
		// 通过 SqlSession 发送 SQL
		Role role = (Role) sqlSession.selectOne("com.corebao.mapper.RoleMapper.getRole", 1L);
		
		// 通过 Mapper 接口发送 SQL
		RoleMapper roleMapper = sqlSession.getMapper(RoleMapper.class);
		Role newRole = new Role();
		newRole.setNote("new2 note");
		newRole.setRoleName("new2 RoleName");
		
		//创建新记录
		int newId = roleMapper.insertRole(newRole);
		sqlSession.commit();
		System.out.println(newId);
		
		//查询新创建的记录
		Role role2 = roleMapper.getRole(newRole.getId());
		System.out.println(role);
		System.out.println(role2.getRoleName());
		
		//删除新创建的记录
		roleMapper.deleteRole(newRole.getId());

		//通过名称模糊查找记录
		List<Role> roles = roleMapper.findRoles("role");
		for (Role r : roles) {
			System.out.println(r.getId() + " name:" + r.getRoleName());
		}

		//多参数及分页
		//实例化分页类
		RowBounds rowBounds = new RowBounds(0,2);
		List<Role> rolesList = roleMapper.findRolesByPage("role", "note", rowBounds);
		System.out.println(rolesList.size());
	}
}
