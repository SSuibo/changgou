package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @PackageName: com.changgou.search.service.impl
 * @ClassName: SearchServiceImpl
 * @Author: suibo
 * @Date: 2020/1/7 15:40
 * @Description: //搜索框微服务实现类
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    //根据搜索框输入的关键字从索引库搜索商品(根据name域进行搜索)
    @Override
    public Map<String,Object> search(Map<String, String> searchMap) {
        Map resultMap = new HashMap();

        if(searchMap!=null){
            //说明用户在搜索框输入了内容,那就要根据用户输入的内容进行关键字搜索
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            //因为后面查询的拼接条件不止一个,所以不能直接用matchQuery,而因该用boolQuery来进行条件的拼接
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            //表示当前构建的是查询操作
            nativeSearchQueryBuilder.withQuery(boolQuery);

            //根据name域关键字进行索引库查询
            if(!StringUtils.isEmpty(searchMap.get("keywords"))){

                //name是根据name域来进行查询,operator代表的是条件的拼接,and相当于&&
                boolQuery.must(QueryBuilders.matchQuery("name",searchMap.get("keywords")).operator(Operator.AND));
            }

            //品牌过滤
            if(!StringUtils.isEmpty(searchMap.get("brand"))){
                boolQuery.filter(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }

            //规格过滤
            for (String key : searchMap.keySet()) {
                if(key.startsWith("spec_")){
                    //如果是以spec_开头的,就说明是规格的相关信息,浏览器的url解析的+是%2B,因此要将+替换为%2B
                    String value = searchMap.get(key).replace("%2B", "+");
                    boolQuery.filter(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }

            //价格区间过滤查询
            if(!StringUtils.isEmpty(searchMap.get("price"))){
                //前台传过来的价格是0-500或500-1000这个格式,索引要进行切割获取String数组,数组里面有两个元素,第一个元素是小的价格
                String[] prices = searchMap.get("price").split("-");
                if(prices.length==2){
                    //说明用户选择的是区间价格(500-1000)
                    boolQuery.filter(QueryBuilders.rangeQuery("price").lte(prices[1]).gte(prices[0]));
                }else {
                    //说明前台是3000以上的价格
                    boolQuery.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));
                }
            }

            //搜索分页查询
            String pageNum = searchMap.get("pageNum");
            String pageSize = searchMap.get("pageSize");
            if(pageNum==null){
                pageNum="1";
            }
            if(pageSize==null){
                pageSize="30";
            }
            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum)-1,Integer.parseInt(pageSize)));

            //排序
            if(!StringUtils.isEmpty(searchMap.get("sortField"))){
                if("ASC".equals(searchMap.get("sortRule"))){
                    //升序
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.ASC));
                }else {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.DESC));
                }
            }

            //高亮显示,设置前后缀
            HighlightBuilder.Field field = new HighlightBuilder.Field("name").preTags("<span style='color:red'>").postTags("</span>");
            nativeSearchQueryBuilder.withHighlightFields(field);

            //品牌聚合(分组)查询
            String skuBrand = "skuBrand";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));

            //规格聚合(分组)查询
            String skuSpec = "skuSpec";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));
            //执行查询
            NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
            //queryForPage需要searchQuery参数,所以在上面构建一个searchQuery参数
            AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(searchQuery, SkuInfo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<T> list = new ArrayList<>();
                    SearchHits hits = searchResponse.getHits();
                    if(hits!=null){
                        for (SearchHit hit : hits) {
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                            if(highlightFields!=null && highlightFields.size()>0){
                                //说明用户输入了高亮显示的关键字
                                skuInfo.setName(highlightFields.get("name").getFragments()[0].toString());
                            }
                            list.add((T) skuInfo);
                        }
                    }
                    return new AggregatedPageImpl<T>(list,pageable,hits.getTotalHits(),searchResponse.getAggregations());
                }
            });

            List<SkuInfo> content = skuInfos.getContent();  //搜索结果内容
            long totalElements = skuInfos.getTotalElements();   //总条数
            int totalPages = skuInfos.getTotalPages();      //总页码
            resultMap.put("rows",content);
            resultMap.put("total",totalElements);
            resultMap.put("totalPages",totalPages);

            //获取品牌聚合结果
            StringTerms brandTerms = (StringTerms) skuInfos.getAggregation(skuBrand);
            List<String> brandList = brandTerms.getBuckets().stream().map((barket) -> barket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("brandList",brandList);

            //获取规格过滤结果
            StringTerms specTerms = (StringTerms) skuInfos.getAggregation(skuSpec);
            List<String> specList = specTerms.getBuckets().stream().map((barket) -> barket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("specList",this.specList(specList));

            //获取分页数据
            resultMap.put("pageNum",pageNum);
            resultMap.put("pageSize",pageSize);
        }
        return resultMap;
    }

    //处理规格集合,将获取到的一坨规格显示到对应的规格行中
    public Map<String, Set<String>> specList(List<String> specList){
        Map<String, Set<String>> specMap = new HashMap<>();
        if(specList!=null && specList.size()>0){
            for (String spec : specList) {
                Map<String,String> map = JSON.parseObject(spec, Map.class);
                for (Map.Entry<String, String> entrySet : map.entrySet()) {
                    String key = entrySet.getKey();
                    String value = entrySet.getValue();
                    Set<String> specValues = specMap.get(key);
                    if(specValues==null){
                        specValues = new HashSet<>();
                    }
                    specValues.add(value);
                    specMap.put(key,specValues);
                }
            }
        }
        return specMap;
    }
}
