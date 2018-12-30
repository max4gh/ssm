package com.corebao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.corebao.pojo.Role;

public interface RoleMapper {
	
	public Role getRole(Long id);
	public int insertRole(Role role);
	public int deleteRole(Long id);
	public int updateRole(Role role);
	public List<Role> findRoles(String roleName);
	
	//使用注解传递多参数及分页
	public List<Role> findRolesByPage(@Param("roleName")String roleName, @Param("note")String note, RowBounds rowBounds);
}
