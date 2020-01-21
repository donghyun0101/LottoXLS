package com.stj.lottoxls;

/*TODO*/
//당첨된 숫자가 출력되는 ListView를 커스텀하여 만들기 위한 adapter에 들어가는 Item List

public class ListViewItem
{
    private String contents;
    private String num1, num2, num3, num4, num5, num6;

    public String getContents()
    {
        return contents;
    }

    public String getNum1()
    {
        return num1;
    }

    public String getNum2()
    {
        return num2;
    }

    public String getNum3()
    {
        return num3;
    }

    public String getNum4()
    {
        return num4;
    }

    public String getNum5()
    {
        return num5;
    }

    public String getNum6()
    {
        return num6;
    }

    public void setContents(String contents)
    {
        this.contents = contents;
    }

    public void setNum1(String num1)
    {
        this.num1 = num1;
    }

    public void setNum2(String num2)
    {
        this.num2 = num2;
    }

    public void setNum3(String num3)
    {
        this.num3 = num3;
    }

    public void setNum4(String num4)
    {
        this.num4 = num4;
    }

    public void setNum5(String num5)
    {
        this.num5 = num5;
    }

    public void setNum6(String num6)
    {
        this.num6 = num6;
    }
}
