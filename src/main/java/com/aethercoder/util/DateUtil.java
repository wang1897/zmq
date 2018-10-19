package com.aethercoder.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Guo Feiyan on 2017/8/30.
 */
public class DateUtil {


    /**
     * 当前时间+1
     *
     * @param date
     * @return
     */
    public static Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, +1);//+1当前时间的时间加一天
        date = calendar.getTime();
        return date;
    }


    /**
     * 当前时间-1
     *
     * @param date
     * @return
     */
    public static Date getBeforeDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -24);//-24当前时间的前一天内
        date = calendar.getTime();
        return date;
    }

    /**
     * 比较两个时间大小
     *
     * @param d1
     * @param d2
     * @return
     */
    public static boolean dateCompare(Date d1, Date d2) {
        if (d1.before(d2)) {
            //表示d1小于d2
            return true;
        } else {
            return false;
        }
    }



    public static Date stringToDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date;
    }


    public static Date stringToDateFormat(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date;
    }

    /**
     * 获取时间戳
     * 输出结果:1438692801766
     */
    public static long getTimeStamp() {
        Date date = new Date();
        long times = date.getTime();
        return times;
        //第二种方法：
        // new Date().getTime();
    }

    /**
     * 获取格式化的时间
     * 输出格式：2017-08-04 20:55:35
     */
    public static String dateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 获取格式化的时间
     * 输出格式：2017-08-04 20:55:35
     */
    public static String dateToStringYYYYMMDD(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 将时间戳转化为标准时间
     * 输出：Tue Oct 07 12:04:36 CST 2014
     */
    public static String timestampToDate(long times) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(times);
        return formatter.format(date);
    }


    /**
     * date2比date1多的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDays(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        //同一年
        if (year1 != year2)
        {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                //闰年
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0)
                {
                    timeDistance += 366;
                } else    //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2 - day1);
        } else    //不同年
        {
            return day2 - day1;
        }
    }

    /**
     * 设定一个日期时间，加几分钟(小时或者天）后得到新的日期
     *
     * @param date
     * @param x    24小时制
     * @return
     */
    public static Date addDateMinut(Date date, Integer x) {
        //引号里面个格式也可以是 HH:mm:ss或者HH:mm等等，很随意的，不过在主函数调用时，要和输入的变
        //量day格式一致
        if (date != null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            // 24小时制
            cal.add(Calendar.MINUTE, x);
            date = cal.getTime();
        }
        return date;

    }


    public  static  Date addDateDay(Date date,Integer day){

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.WEDNESDAY, -1);
        return  cal.getTime();
    }

    /**
     * 获取上月时间的当天
     * @param date
     * @return
     */
    public  static Date getLastMouth(Date date){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // 设置为当前时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); // 设置为上一个月
        date = calendar.getTime();
        return date;
    }

    /**
     * 上月第1天
     * @param date
     * @return
     */
    public  static Date getLastMouthFirstDay(Date date)  {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        // GregorianCalendar是Calendar的一个具体子类
        GregorianCalendar gc1 =(GregorianCalendar)Calendar.getInstance();
        gc1.setTime(date);
        gc1.set(Calendar.DAY_OF_MONTH,  1 );      //设置该月的第一天
        gc1.set(Calendar.HOUR_OF_DAY,0);
        gc1.set(Calendar.MINUTE,0);
        gc1.set(Calendar.SECOND,0);
        return gc1.getTime();
    }

    /**
     * 获取指定时间在本年中的周次
     * @param date
     * @return
     */
    public  static Long getWeekthByDate(Date date)  {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        int i = ca.get(Calendar.WEEK_OF_YEAR);
        Long aLong = new Long(i);
        return aLong;
    }

    /**
     * 获取指定时间在本年中的月次
     * @param date
     * @return
     */
    public  static Long getMonthByDate(Date date)  {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        int i = ca.get(Calendar.MONTH);
        Long aLong = new Long(i);
        return aLong;
    }

    /**
     * 获取指定时间的年份
     * @param date
     * @return
     */
    public  static Long getYearOfDate(Date date)  {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        int i = ca.get(Calendar.YEAR);
        Long aLong = new Long(i);
        return aLong;
    }
}



