package com.ycbd.photoservice.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ycbd.photoservice.services.SchedulerService;
import com.ycbd.photoservice.services.SystemService;
import com.ycbd.photoservice.tools.ResultData;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Service
@Api(value = "system", tags = "系统处理模块")
@RequestMapping("system/")
@RestController
@CrossOrigin(origins = "*")
public class SystemController {
    @Resource
    protected SystemService service;
	@Resource
    protected SchedulerService scheduleJob;

	 @ApiOperation(value = "定时任务增加", notes = "获取指定目录文件信息")
    @PostMapping(value = "/scheduleJob")
    @ResponseBody
    public ResultData<Boolean> scheduleJob(@RequestBody Map<String,Object> taskMap) throws IOException {
        boolean result=false;
        try {
            scheduleJob.addJob(taskMap);
            result=true;
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ResultData.success(result);
    }
    @ApiOperation(value = "获取菜单数据", notes = "获取菜单数据")
	@PostMapping(value = "/getMenuTree")
	@ResponseBody
	public ResultData<List<Map<String, Object>>> getMenuTree() {
		List<Map<String, Object>> result = service.getMenuTree();
		Iterator<Map<String, Object>> iterator = result.iterator();
		while (iterator.hasNext()) {
			Map<String, Object> map = iterator.next();
			JSONObject jsonObject = (JSONObject) JSONUtil.parse(MapUtil.getStr(map, "menu"));
			if (jsonObject.containsKey("icon") && MapUtil.getInt(jsonObject, "icon") == 0
					&& (jsonObject.get("childrens") == null || ((List) jsonObject.get("childrens")).isEmpty())) {
				iterator.remove();
			}
		}

		return ResultData.success(result);
	}

    @ApiOperation(value = "数据标题", notes = "数据标题")
	@PostMapping(value = "/getHeaderData")
	@ResponseBody
	public ResultData<List<Map<String,Object>>> getHeaderData(String table) {
		try {
			// String headerData = JSONUtil.toJsonStr(service.getHeaderData(table));
			return ResultData.success(service.getHeaderData(table));
		} catch (Exception e) {
			return ResultData.fail(100, e.getMessage());
		}
	}
	@ApiOperation(value = "树形数据", notes = "树形数据")
	@PostMapping(value = "/getTreeData")
	@ResponseBody
	public ResultData<List<Map<String,Object>>> getTreeData(String table) {
		try {
			// String headerData = JSONUtil.toJsonStr(service.getHeaderData(table));
			return ResultData.success(service.getTreeData(table));
		} catch (Exception e) {
			return ResultData.fail(100, e.getMessage());
		}
	}
	@ApiOperation(value = "查询数据", notes = "查询数据")
	@PostMapping(value = "/getSearchData")
	@ResponseBody
	public ResultData<List<Map<String,Object>>> getSearchData(String table,String whereString) {
		try {
			// String headerData = JSONUtil.toJsonStr(service.getHeaderData(table));
			return ResultData.success(service.getData(table,whereString));
		} catch (Exception e) {
			return ResultData.fail(100, e.getMessage());
		}
	}
	@ApiOperation(value = "获取位置数据", notes = "数据标题")
	@PostMapping(value = "/getPlaceData")
	@ResponseBody
	public ResultData<List<Map<String, Object>>> getPlaceData(String place) {
		try {
			String table="gpsinfo";
			String whereString="";
			if(!StrUtil.isEmpty(place))
			whereString=" place_name like '%"+place+"%'";
			List<Map<String, Object>> resuList=service.getData(table,whereString);
			return ResultData.success(resuList);
		} catch (Exception e) {
			return ResultData.fail(100, e.getMessage());
		}
	}

	@ApiOperation(value = "获取数据总数", notes = "获取数据总数")
	@PostMapping(value = "/getDataCount")
	@ResponseBody
	public ResultData<Integer> getDataCount(String table, String params) {
		try {
			int total = service.getDataCount(table, params);
			return ResultData.success(total);
		} catch (Exception e) {
			return ResultData.fail(100, e.getMessage());
		}

	}

	

	@ApiOperation(value = "getDatas", notes = "getDatas")
	@PostMapping(value = "/getDatas")
	@ResponseBody
	public ResultData<List<Map<String, Object>>> getDatas(String table, @RequestBody Map<String, Object> request) {
		List<Map<String, Object>> items = new ArrayList<>();
		try {
			int pageIndex = MapUtil.getInt(request, "currentPage", 0);
			int pageSize = MapUtil.getInt(request, "pageSize", 20);
			String sortByString = MapUtil.getStr(request, "sort", "");
			String groupByString = MapUtil.getStr(request, "group", "");
			boolean treeFlag = MapUtil.getBool(request, "treeflag", false);
			Map<String, Object> pMap = new HashMap<>();
			Object whStringObj = request.get("whString");
			if (whStringObj != null && whStringObj instanceof Map) {
				pMap = (Map<String, Object>) whStringObj;
			}
			items = service.getDatas(table, pageIndex, pageSize, sortByString, groupByString, pMap);
			if (treeFlag)
				return ResultData.success(service.constructTree(items));
			else
				return ResultData.success(items);
		} catch (Exception e) {
			return ResultData.fail(100, e.getMessage());
		}
	}

	@ApiOperation(value = "getItemsData", notes = "getItemsData")
	@PostMapping(value = "/getItemsData")
	@ResponseBody
	public ResultData<List<Map<String, Object>>> getItemsData(String table, int pageIndex, int pageSize,
			String whereStr, String sortByAndType, String groupbyString) {
		try {
			List<Map<String, Object>> items = service.getItemsData(table, pageIndex, pageSize, whereStr, sortByAndType,
					groupbyString);
			return ResultData.success(items);
		} catch (Exception e) {

			return ResultData.fail(100, e.getMessage());
		}

	}

	@ApiOperation(value = "getFormData", notes = "获取编辑提交数据")
	@PostMapping(value = "/getFormData")
	@ResponseBody
	public ResultData<List<Map<String, Object>>> getFormData(String table) {

		try {

			List<Map<String, Object>> items = service.getEditData(table);
			return ResultData.success(items);
		} catch (Exception e) {
			return ResultData.fail(100, e.getMessage());
		}
	}

	@ApiOperation(value = "新增数据", notes = "新增保存数据")
	@PostMapping(value = "/addSave")
	@ResponseBody
	public ResultData addSave(String table, String params) {
		try {
			Map<String, Object> map = JSONUtil.parseObj(params).toBean(Map.class);

			int saveResult = service.addSave(table, map);
			return ResultData.success(saveResult);

		} catch (Exception ex) {
			return ResultData.fail(100, ex.getMessage());
		}

	}

	@ApiOperation(value = "删除数据", notes = "删除数据")
	@PostMapping(value = "/delData")
	@ResponseBody
	public ResultData delData(String table, String idString) {
		try {

			service.delData(table, idString);
			return ResultData.success(1);

		} catch (Exception ex) {
			return ResultData.fail(105, "删除数据不成功，错误信息：" + ex.getMessage());
		}

	}

	@ApiOperation(value = "保存数据", notes = "保存数据")
	@PostMapping(value = "/save")
	@ResponseBody
	public ResultData save(String table, String params, int id) {
		// Create an instance of ObjectMapper
		int saveResult = 0;
		
		try {
			// Convert JSON String to a Map<String, Object>
            Map<String, Object> map = JSONUtil.parseObj(params).toBean(Map.class);
			if (MapUtil.isEmpty(map)) {
				return ResultData.fail(101, "更新的数据不能为空");
			}
			try {
				saveResult = service.updateData(table, "id", map, id);
			} catch (Exception e) {
				return ResultData.fail(103, e.getCause().getMessage());
			}
			if (saveResult < 0)
				return ResultData.fail(100, "");

			return ResultData.success(saveResult);

		} catch (Exception e) {
			return ResultData.fail(100, e.getMessage());
		}

	}
    
}
