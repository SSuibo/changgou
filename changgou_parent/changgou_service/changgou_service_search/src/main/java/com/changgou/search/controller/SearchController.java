package com.changgou.search.controller;

import com.changgou.entity.Page;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @PackageName: com.changgou.search.controller
 * @ClassName: SearchController
 * @Author: suibo
 * @Date: 2020/1/7 16:14
 * @Description: //搜索的controller
 */
@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    public SearchService searchService;

    /*@GetMapping
    public Map search(@RequestParam Map<String,String> paramMap){
        return searchService.search(paramMap);
    }*/

    @GetMapping("/list")
    public String search(@RequestParam Map<String,String> searchMap, Model model){
        this.handerSearchMap(searchMap);
        Map<String, Object> resultMap = searchService.search(searchMap);
        model.addAttribute("resultMap",resultMap);
        //封装searchMap的数据是为了回显数据
        model.addAttribute("searchMap",searchMap);

        //设置分页的相关数据
        Page<SkuInfo> page = new Page<SkuInfo>(
                Long.parseLong(String.valueOf(resultMap.get("total"))),
                Integer.parseInt(String.valueOf(resultMap.get("pageNum"))),
                Integer.parseInt(String.valueOf(resultMap.get("pageSize")))
                );      //设置分页的相关数据,总条数,当前页,每页显示记录数
        model.addAttribute("page",page);

        //拼装url
        StringBuilder url = new StringBuilder("/search/list");
        if(searchMap!=null && searchMap.size()>0){
            //拼装查询条件
            url.append("?");
            for (String paramKey : searchMap.keySet()) {
                if(!"sortRule".equals(paramKey) && !"sortField".equals(paramKey) && !"pageNum".equals(paramKey)){
                    url.append(paramKey).append("=").append(searchMap.get(paramKey)).append("&");
                }
            }

            String urlString = url.toString();
            //去除路径上的最后一个&
            urlString = urlString.substring(0,urlString.length()-1);
            model.addAttribute("url",urlString);
        }else{
            model.addAttribute("url",url);
        }
        return "search";
    }

    //浏览器中+在地址栏显示的是%2B,所以要将+进行替换
    public void handerSearchMap(Map<String,String> searchMap){
        if(searchMap!=null && searchMap.size()>0){
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if(entry.getKey().startsWith("spec_")){
                    searchMap.put(entry.getKey(),entry.getValue().replace("+","%2B"));
                }
            }
        }
    }
}
