package nju.SEIII.EASIEST.utilities.VO;

import lombok.Data;

/**
 * @title: outputVO
 * @Author: Stanton JoY
 * @Date: 2023/4/7 15:22
 */
@Data
public class OutputVO {
    int iPos;
    int iNeg;
    int iTrinary;
    int iScale;
    String outputMessage;

    public OutputVO() {
        this.iPos = 1;
        this.iNeg = 1;
        this.iTrinary = 0;
        this.iScale = 0;
        this.outputMessage="";
    }
}
