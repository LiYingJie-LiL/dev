package com.sky.service.impl;

import com.github.pagehelper.dialect.helper.HsqldbDialect;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.DataTruncation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;



    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList=new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            //日期计算，计算指定日期的后一天对应的日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天营业额
        List turnoverList=new ArrayList<>();
        for(LocalDate date:dateList){
            //查询date日期对应的营业额，营业额是指：状态为"已完成"的订单金额合计
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);//LocalTime.MIN 00:00
            LocalDateTime endTime=LocalDateTime.of(date,LocalTime.MAX);//LocalTime.MAX 指当天最后的时间

            //select sum(amount) from orders where order_time>beginTime and order_time<endTime and status=5
            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            turnover=turnover==null?0.0:turnover;//因为如果当天营业额为空，其实是一个空集。直接转化为字符串明显有问题
            turnoverList.add(turnover);
        }

        //StringUtils.join(dateList,",");//将dateList集合转化成字符串，并且每个数据单独用逗号隔开

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList=new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            //日期计算，计算指定日期的后一天对应的日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每一天的用户新增数量  select count(id) from user where create_time< ? and create_time >？
        List<Integer> newUserList=new ArrayList<>();
        //存放每一天的用户总数量 select count(id) from user where create_time< ?
        List<Integer> totalUserList=new ArrayList<>();

        for (LocalDate date:dateList){
            LocalDateTime beginTime=LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date,LocalTime.MAX);

            Map map=new HashMap();
            map.put("end",endTime);

            //总用户数量
            Integer totalUser=userMapper.countByMap(map);

            map.put("begin",beginTime);
            //新增用户数量
            Integer newUser=userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        //封装结果
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }


    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList=new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天订单总数
        List<Integer> orderCountList=new ArrayList();
        //存放每天的有效订单总数
        List<Integer> validOrderCountList=new ArrayList<>();


        //遍历dateList集合，查询每天的有效订单数和订单总数
        for(LocalDate date:dateList){
            LocalDateTime beginTime=LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date,LocalTime.MAX);
            //查询每天的订单总数 select count(id) from order_time> ? and order_time <?
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            //查询每天的有效订单数 select count(id) from order_time> ? and order_time <? and status=5 (已完成)
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);

        }

        //计算时间区间内的订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //计算订单完成率
        Double orderCompletionRate=0.0;
        if(totalOrderCount!=0){
            orderCompletionRate=validOrderCount.doubleValue()/totalOrderCount;
        }

        //封装结果
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .orderCompletionRate(orderCompletionRate)
                .build();

    }


    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    public Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map=new HashMap();
        map.put("begin",begin );
        map.put("end",end);
        map.put("status",status);
        Integer i = orderMapper.countByMap(map);
        return i;

    }


    /**
     * 根据条件统计销量前十
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        // 1. 遍历集合salesTop10，取出每个GoodsSalesDTO的name，封装成List<String>
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        // 2. 把name集合用逗号拼接成一整个字符串，例："可乐,雪碧,矿泉水"
        String nameList = StringUtils.join(names, ",");

        // 3. 遍历集合salesTop10，取出每个GoodsSalesDTO的number（销量），封装成List<Integer>
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        // 4. 把销量集合用逗号拼接成一整个字符串，例："120,98,65"
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出Excel运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessDate(HttpServletResponse response) throws Exception{

        //1.查询数据库，获取营业数据 ————查询最近30天的运营数据
        LocalDate dateBegin=LocalDate.now().minusDays(30);
        LocalDate dateEnd=LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2.通过POI将数据写入到Excel文件
         //先创建一个输入流
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
         //基于模板文件创建一个新的Excel文件
        XSSFWorkbook excel=new XSSFWorkbook(in);
        //获取表格标签页sheet
        XSSFSheet sheet1 = excel.getSheet("Sheet1");
        //填充数据-- 时间
        sheet1.getRow(1).getCell(1).setCellValue("时间："+dateBegin+"至"+dateEnd);
        //获得第4行
        XSSFRow row = sheet1.getRow(3);
        row.getCell(2).setCellValue(businessDataVO.getTurnover());
        row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
        row.getCell(6).setCellValue(businessDataVO.getNewUsers());

        //获得第5行
        row = sheet1.getRow(4);
        row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
        row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

        //填充明细数据
        for(int i=0;i<30;i++){
            LocalDate date=dateBegin.plusDays(1);
            //查询某一天的营业数据
            BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
            //获得某一行
            row = sheet1.getRow(7 + i);
            row.getCell(1).setCellValue(date.toString());
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(3).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(5).setCellValue(businessData.getUnitPrice());
            row.getCell(6).setCellValue(businessData.getNewUsers());


        }

        //3.通过输出流将Excel文件下载到客户端浏览器
        ServletOutputStream out = response.getOutputStream();
        excel.write(out);

        //4.关闭资源
        out.close();
        excel.close();

    }

}
