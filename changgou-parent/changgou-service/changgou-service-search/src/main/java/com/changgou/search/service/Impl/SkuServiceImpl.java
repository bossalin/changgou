package com.changgou.search.service.Impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author wzq on 2020/11/26.
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /**
     * ElasticsearchTemplate:可以实现索引库的增删改查【高级搜索】
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     *crtl + Alt +M :抽取方法
     */

    @Override
    public Map search_groud(Map<String, String> searchMap) {

        //1.获取关键字的值
        String keywords = searchMap.get("keywords");

        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";//赋值给一个默认的值
        }
        //2.创建查询对象 的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //3.设置查询的条件

        //设置分组条件  商品分类
        /**
         *         addAggregation添加一个聚合操作
         *         .terms：取别名
         *         .field:根据那个域进行分组
         */


        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
//        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));

        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //5.执行查询
        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(query, SkuInfo.class);

        //获取分组结果
        StringTerms stringTerms = (StringTerms) skuPage.getAggregation("skuCategorygroup");

        List<String> categoryList = new ArrayList<>();

        if (stringTerms != null) {
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();//分组的值
                categoryList.add(keyAsString);
            }
        }
        //6.返回结果
        Map resultMap = new HashMap<>();
        resultMap.put("categoryList", categoryList);
        resultMap.put("rows", skuPage.getContent());
        resultMap.put("total", skuPage.getTotalElements());
        resultMap.put("totalPages", skuPage.getTotalPages());

        return resultMap;
    }

    @Override
    public Map search(Map<String, String> searchMap) {


        /**
         * 执行搜索，响应结果给我
         * 1）搜索条件封装对象
         * 2)搜索的结果集（集合数据）需要转换的类型
         * 3）AggregatedPage<SkuInfo>：搜索结果集的封装
         *
         */


        //2.创建查询对象 的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //3.设置查询的条件
        if (searchMap!=null && searchMap.size()>0) {
            String keywords = searchMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)){
//                nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));
        nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
            }
        }
        //高亮配置
        HighlightBuilder.Field field=new HighlightBuilder.Field("name");//指定高亮域
        //前缀
        field.preTags("<em style=\"color:red;\">");
        //后缀 </em>
        field.postTags("</em>");
        //碎片长度   关键词数据的长度   不超100
        field.fragmentOffset(100);
        //添加高亮
        nativeSearchQueryBuilder.withHighlightFields(field);

        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //5.执行查询（分页信息）
//        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(query, SkuInfo.class);
        AggregatedPage<SkuInfo> skuPage=elasticsearchTemplate.queryForPage(
                query,                //搜索条件封装
                SkuInfo.class,          //数据集合要转换的类型字节码
                //SearchResultMapper           //执行搜索后，将数据结果封装到该对象中
                new SearchResultMapper() {
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                        /**
                         *   高亮流程：--------
                         *
                         *   执行查询，获取所有数据  ->结果集 【非高亮数据|高亮数据】
                         *
                         *   分析结果集数据，获取非高亮数据
                         *
                         *   分析结果集数据，获取高亮数据 ->只有某个域的高亮数据
                         *
                         *   非高亮数据中自定的域替换成高亮数据
                         *
                         *   将数据返回
                         */
                        //存储所有转换后的高亮数据对象
                        List<T> list=new ArrayList<T>();
                        //执行查询，获取所有数据  ->结果集 【非高亮数据|高亮数据】
                        for (SearchHit hit: searchResponse.getHits()) {
                            //分析结果集数据，获取非高亮数据
                            SkuInfo skuInfo=JSON.parseObject(hit.getSourceAsString(),SkuInfo.class);
                            //分析结果集数据，获取高亮数据 -》只有某个域的高亮数据
                            HighlightField highlightField = hit.getHighlightFields().get("name");
                            if (highlightField!=null && highlightField.getFragments()!=null){
                                //高亮数据读出来
                                Text[] fragments = highlightField.getFragments();
                                StringBuffer stringBuffer = new StringBuffer();
                                for (Text fragment:fragments){
                                        stringBuffer.append(fragment.toString());
                                }
                                //非高亮数据中指定的域替换成高亮数据
                                skuInfo.setName(stringBuffer.toString());
                            }
                            //将高亮数据添加到集合
                            list.add((T) skuInfo);
                        }

                    //将数据返回
                        return new AggregatedPageImpl<T>(list,pageable,searchResponse.getTotalShards());
                    }

                    @Nullable
                    @Override
                    public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                        return null;
                    }
                }
        );






        //6.返回结果
        Map resultMap = new HashMap<>();
        //获取数据结果集
        resultMap.put("rows", skuPage.getContent());
        //总记录数
        resultMap.put("total", skuPage.getTotalElements());
        //总页数
        resultMap.put("totalPages", skuPage.getTotalPages());

        return resultMap;
    }






    /**
     * 导入sku数据到es
     */
    @Override
    public void importSku(){
        //调用changgou-service-goods微服务
        Result<List<Sku>> skuListResult = skuFeign.findByStatus("1");
//        Result<List<Sku>> skuListResult=skuFeign.findAll();
        /**
         * 将List<Sku>转换成List<SkuInfo>
         * List<Sku>  --> [{skuJSON}]  --> List<SkuInfo>
         */
        List<SkuInfo> skuInfos=  JSON.parseArray(JSON.toJSONString(skuListResult.getData()),SkuInfo.class);
        for(SkuInfo skuInfo:skuInfos){
            Map<String, Object> specMap= JSON.parseObject(skuInfo.getSpec()) ;
            //如果需要生成动态的域，只需要将于存入到一个Map<String,Object>对象中即可，该Map<String,Object>的key会生成一个域，遇到名字为该map的key
            //当前Map<String,Object>后面的Object的值会作为当前Sku对象该域（key）对应的值
            skuInfo.setSpecMap(specMap);

        }
        //springboot版本问题
        skuEsMapper.saveAll(skuInfos);
    }





    public Map search_new(Map<String, String> searchMap) {
//
//        //1.获取关键字的值
//        String keywords = searchMap.get("keywords");

//        if (StringUtils.isEmpty(keywords)) {
//            keywords = "华为";//赋值给一个默认的值
//        }
        //2.创建查询对象 的构建对象
//        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        NativeSearchQueryBuilder nativeSearchQueryBuilder=buildBasicQuery(searchMap);
        //3.设置查询的条件

//        //设置分组条件  商品分类
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
//
//        //设置分组条件  商品品牌
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(50));

//设置分组条件  商品的规格
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(100));

//        if (!StringUtils.isEmpty(keywords)) {
//            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));
//        }
////        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));

//
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
//            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
//        }else {
//            //设置分组条件  商品品牌
//            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(50));
//
//        }
//
//        if (!StringUtils.isEmpty(searchMap.get("category"))) {
//            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
//        }else{
//            //设置分组条件  商品的规格
//            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
//        }
//
//
//        //构建过滤查询
//        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);


        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //5.执行查询
        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(query, SkuInfo.class);

        //获取分组结果  商品分类
        StringTerms stringTermsCategory = (StringTerms) skuPage.getAggregation("skuCategorygroup");
        //获取分组结果  商品品牌
        StringTerms stringTermsBrand = (StringTerms) skuPage.getAggregation("skuBrandgroup");
        //获取分组结果  商品规格
        StringTerms stringTermsSpec = (StringTerms) skuPage.getAggregation("skuSpecgroup");

        List<String> categoryList = getStringsList(stringTermsCategory);

        List<String> brandList = getStringsList(stringTermsBrand);

        Map<String, Set<String>> specMap = getStringSetMap(stringTermsSpec);

        Integer pageNum=coverterPage(searchMap);
        Integer pageSize=10;
        //6.返回结果
        Map resultMap = new HashMap<>();


        resultMap.put("categoryList", categoryList);
        resultMap.put("brandList", brandList);
        resultMap.put("specList",specMap);
        resultMap.put("rows", skuPage.getContent());
        resultMap.put("total", skuPage.getTotalElements());
        resultMap.put("totalPages", skuPage.getTotalPages());
        resultMap.put("pageNum",pageNum);
        resultMap.put("pageSize",pageSize);
        return resultMap;
    }
    /**
     * 获取分组数据列表
     *
     * @param stringTermsBrand
     * @return
     */
    private List<String> getStringsList(StringTerms stringTermsBrand) {
        List<String> list = new ArrayList<>();
        if (stringTermsBrand != null) {
            for (StringTerms.Bucket bucket : stringTermsBrand.getBuckets()) {
                list.add(bucket.getKeyAsString());
            }
        }
        return list;
    }


    /**
     * 获取规格列表数据
     *
     * @param stringTermsSpec
     * @return
     */
    private Map<String, Set<String>> getStringSetMap(StringTerms stringTermsSpec) {
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specList = new HashSet<>();
        if (stringTermsSpec != null) {
            for (StringTerms.Bucket bucket : stringTermsSpec.getBuckets()) {
                specList.add(bucket.getKeyAsString());
            }
        }
        for (String specjson : specList) {
            Map<String, String> map = JSON.parseObject(specjson, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {//
                String key = entry.getKey();        //规格名字
                String value = entry.getValue();    //规格选项值
                //获取当前规格名字对应的规格数据
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<String>();
                }
                //将当前规格加入到集合中
                specValues.add(value);
                //将数据存入到specMap中
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }


    /**
     *
     * @param searchmap
     * @return
     */
    public NativeSearchQueryBuilder buildBasicQuery(Map<String,String> searchmap){
        NativeSearchQueryBuilder nativeSearchQueryBuilder=new NativeSearchQueryBuilder();

        //BoolQuery  组合条件   must,must_not,should
        BoolQueryBuilder boolQueryBuilder=QueryBuilders.boolQuery();


        if (searchmap!=null && searchmap.size()>0){
            String keywords=searchmap.get("keywords");
            if (!StringUtils.isEmpty(keywords)){
//                nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));

            }

            //输入分类 -> category
            if (!StringUtils.isEmpty(searchmap.get("category"))){
                 boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",searchmap.get("category")));
            }

            //输入品牌 ->brand
            if (!StringUtils.isEmpty(searchmap.get("brand"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",searchmap.get("brand")));

            }

        }
        for (Map.Entry entry: searchmap.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith("spec_")){
                //规格条件的值
                String value= (String) entry.getValue();
                boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
            }
        }
        String price=searchmap.get("price");
        if (!StringUtils.isEmpty(price)){
            price.replace("元","").replace("以上","");
            String[] prices=price.split("-");
            if (prices!=null && prices.length>0){
                System.out.println(Integer.valueOf(prices[0]));
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.valueOf(prices[0])));
                if(prices.length==2){
                    System.out.println(Integer.valueOf(prices[1]));
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.valueOf(prices[1])));
                }
            }
        }
        //排序实现
        String sortField=searchmap.get("sortField");  //指定排序的域
        String sortRule=searchmap.get("sortRule");  //指定排序的规则
        if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)){
            nativeSearchQueryBuilder.withSort(new FieldSortBuilder(sortField)  //指定排序域
                    .order(SortOrder.valueOf(sortRule)));   //指定排序规则
        }


        //分页，用户如果不传分页参数，则默认第1页
        Integer pageNum = coverterPage(searchmap); //默认第1页
        Integer size=10;  //默认查询数据条数
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum,size));

        //将boolQueryBuilder填充给nativeSearchQueryBuilder
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    /**
     * 接收前端传入的分页参数
     * @param searchmap
     * @return
     */
    public Integer coverterPage(Map<String,String> searchmap){
        if (searchmap!=null && searchmap.size()>0){
            String pageNum = searchmap.get("pageNum");
            if(pageNum!=null){
                return Integer.valueOf(pageNum);
            }
        }
        return 1;
    }

    /**
     * 分组查询  -> 分类分组 、品牌分组 、规格分组
     * @param nativeSearchQueryBuilder
     * @return
     */
//    public List<String> searchCroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder){
//              //设置分组条件  商品分类
//        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
////        //设置分组条件  商品品牌
//        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(50));
////设置分组条件  商品的规格
//        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(100));
//        //4.构建查询对象
//        NativeSearchQuery query = nativeSearchQueryBuilder.build();
//
//        //5.执行查询
//        AggregatedPage<SkuInfo> skuPage = elasticsearchTemplate.queryForPage(query, SkuInfo.class);
//
//        //获取分组结果  商品分类
//        StringTerms stringTermsCategory = (StringTerms) skuPage.getAggregation("skuCategorygroup");
//        //获取分组结果  商品品牌
//        StringTerms stringTermsBrand = (StringTerms) skuPage.getAggregation("skuBrandgroup");
//        //获取分组结果  商品规格
//        StringTerms stringTermsSpec = (StringTerms) skuPage.getAggregation("skuSpecgroup");
//        List<String> categoryList = getStringsList(stringTermsCategory);
//        List<String> brandList = getStringsList(stringTermsBrand);
//        List<String> specList = getStringsList(stringTermsSpec);
//
//    }

}