// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   Test.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

/**
 * This is a test class that demonstrates detecting ASCII strings and doing URL encoding.
 */
public class Test
{

    public Test()
    {
    }

    public static void main(String[] args)
    {
        CharsetEncoder asciiEncoder = StandardCharsets.US_ASCII.newEncoder();
        String test = "R\351al";
        System.out.println(test + " isPureAscii() : " + asciiEncoder.canEncode(test));
        for(int i = 0; i < test.length(); i++)
            if(!asciiEncoder.canEncode(test.charAt(i)))
                System.out.println(test.charAt(i) + " isn't Ascii() : ");

        test = "Real";
        System.out.println(test + " isPureAscii() : " + asciiEncoder.canEncode(test));
        test = "a\u2665c";
        System.out.println(test + " isPureAscii() : " + asciiEncoder.canEncode(test));
        for(int i = 0; i < test.length(); i++)
            if(!asciiEncoder.canEncode(test.charAt(i)))
                System.out.println(test.charAt(i) + " isn't Ascii() : ");

        System.out.println("Encoded Word = " + URLEncoder.encode(test));
    }
}
