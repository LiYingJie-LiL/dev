package com.sky.controller.admin;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 统计接口
 */
@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "统计相关接口")
public class ReportController {

    @Autowired
    private ReportService reportService;




    /**
     *营业额统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
            ){
        log.info("营业额数据统计：{}，{}",begin,end);
        TurnoverReportVO turnoverStatistics = reportService.getTurnoverStatistics(begin, end);
        return Result.success(turnoverStatistics);
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("用户数据统计：{},{}",begin,end);
        UserReportVO userStatistics = reportService.getUserStatistics(begin, end);
        return Result.success(userStatistics);

    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){
        log.info("订单统计：{}",begin,end);
        OrderReportVO orderStatistics = reportService.getOrderStatistics(begin, end);
        return Result.success(orderStatistics);
    }


    /**
     *销量top1统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> salesTop10Statistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){
        log.info("销量top1统计：{}",begin,end);

        SalesTop10ReportVO salesTop10Statistics = reportService.getSalesTop10Statistics(begin, end);

        return Result.success(salesTop10Statistics);}


    /**
     * 导出Excel运营数据报表
     * @param response
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception{
        //声明一个response对象来获取输出流
        reportService.exportBusinessDate(response);
    }



}
