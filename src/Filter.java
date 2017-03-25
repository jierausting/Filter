import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2016/10/19 0019.
 */
public class Filter {
    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("d:\\DIP\\84.png"));
        int[][] filter = new int[3][3];
        //////////////////////////////////////////
        /*平滑过滤器*/
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                    filter[i][j] = 1;
                }
            }
////////////////////////////////////////////////////////////////
        /*拉普拉斯的过滤器
/*        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) {
                    filter[i][j] =  -8;
                } else {
                    filter[i][j] = 1;
                }
            }
        }*/
        //////////////////////////////////////////////////
        image = filter2d(image, filter,0.2);
        ImageIO.write(image, "png", new File("d:\\DIP\\84_filter_highBoost.png"));
    }


//////////////////////////////////////////////////////////////////////////
    /*没有加double k 是用来处理平滑滤波器和拉普拉斯滤波器的*/
    public static BufferedImage filter2d(BufferedImage image,int[][] filter) {
        BufferedImage newImage = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        int width = image.getWidth();
        int height = image.getHeight();
        int i = 0;/*i和j是用来对应到整幅图的image的，用来遍历每个像素点，循环需要*/
        int j = 0;/**/
        int a = 0;/*a和b则是用来进行卷积时对那个模板进行遍历的，一样使循环需要*/
        int b = 0;/**/
        int c;/*用于拉普拉斯滤波时判断是加上滤波结果还是减去滤波结果*/
        int internal = filter[0].length/2;
        int sum = 0;/*卷积时用来存乘积求和的结果*/
        int sumToInt = 0;/*把除后的double转回int*/
        double temp = 0.0;/*这个是用来处理平滑滤波的，乘积求和后要把整个结果除以模板边长的平方，所以为double*/
        int tempRgb;
        int checkType = 0;/*用来判断滤波器类型，如果模板全部数值加起来为0则为拉普拉斯滤波器*/
        for (;i<width;i++) {
            for (j = 0;j <height;j++) {
                sum = 0;
                checkType = 0;
                for(a = (-1)*internal; a<=internal; a++) {
                    for(b = (-1)*internal; b<=internal;b++) {
                        checkType += filter[a+internal][b+internal];
                        if((i+a<0)||(i+a>=width)||(j+b<0)||(j+b>=height)) {/*超出边界的处理，相当于0-padding*/
                            tempRgb = 0;
                        }
                        else {
                            tempRgb = image.getRGB(i+a,j+b)&0x0000FF;
                            sum += filter[a+internal][b+internal]*tempRgb;
                        }
                    }
                }

                if ((int)checkType == (filter[0].length)*(filter[0].length)) {
                    temp = (double) sum/(filter[0].length*filter[0].length);
                    sumToInt = (int)temp;
                    sumToInt = sumToInt&0x0000FF;
                    sumToInt = sumToInt<<16|sumToInt<<8|sumToInt;
                    newImage.setRGB(i,j,sumToInt);
                }
                else {
                    sumToInt = (int)sum;
                    sumToInt = (sumToInt<0)?0:sumToInt;/*处理负值*/
                    c =filter[internal][internal]>0?1:(-1);
                    sumToInt = image.getRGB(i,j)&0x0000FF+(c)*sumToInt;/*锐化*/
                    sumToInt = (sumToInt>255)?255:sumToInt;/*标定*/
                    sumToInt = sumToInt<<16|sumToInt<<8|sumToInt;
                    newImage.setRGB(i,j,sumToInt);
                }
            }
        }
        return newImage;
    }
    /////////////////////////////////////////////////////////////////
    /*加了double k 的重载是用来处理高提升滤波和非锐化掩蔽的，和上面的方法实现基本相同*/
    public static BufferedImage filter2d(BufferedImage image,int[][] filter,double k) {
        BufferedImage newImage = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        int width = image.getWidth();
        int height = image.getHeight();
        int i = 0;
        int j = 0;
        int a = 0;
        int b = 0;
        int c;
        int internal = filter[0].length/2;
        int sum = 0;
        int sumToInt = 0;
        double temp = 0.0;
        int tempRgb;
        int delta;/*用来存取原图和平滑后图像的差值*/
        double dstRgb_temp = 0.0;/*由于k*delta是个double，为了保证数据的舍去误差不会太大，所以用double存取f(x,y)+k*gmask*/
        int dstRgb = 0;
        double srcRgb;/*获取原图*/
        int checkType = 0;
        for (;i<width;i++) {
            for (j = 0;j <height;j++) {
                sum = 0;
                checkType = 0;
                for(a = (-1)*internal; a<=internal; a++) {
                    for(b = (-1)*internal; b<=internal;b++) {
                        checkType += filter[a+internal][b+internal];
                        if((i+a<0)||(i+a>=width)||(j+b<0)||(j+b>=height)) {
                            tempRgb = 0;
                        }
                        else {
                            tempRgb = image.getRGB(i+a,j+b)&0x0000FF;
                            sum += filter[a+internal][b+internal]*tempRgb;
                        }
                    }
                }

                if ((int)checkType == (filter[0].length)*(filter[0].length)) {
                    temp = (double) sum/(filter[0].length*filter[0].length);
                    sumToInt = (int)temp;
                   delta = image.getRGB(i,j)&0x0000FF-sumToInt;/*求gmask*/
                    dstRgb_temp = k*delta;
                    srcRgb = image.getRGB(i,j)&0x0000FF;
                    dstRgb = (int)(dstRgb_temp+srcRgb);/*目标值*/
                    dstRgb = (dstRgb<0)?0:dstRgb;/*负值的处理*/
                    dstRgb = (dstRgb>255)?255:dstRgb;/*超出255的处理*/
                    dstRgb = dstRgb<<16|dstRgb<<8|dstRgb;
                    newImage.setRGB(i,j,dstRgb);
                }
                else {
                    sumToInt = (int)sum;
                    sumToInt = (sumToInt<0)?0:sumToInt;
                    c =filter[internal][internal]>0?1:(-1);
                    sumToInt = image.getRGB(i,j)&0x0000FF+(c)*sumToInt;
                    sumToInt = (sumToInt>255)?255:sumToInt;
                    sumToInt = sumToInt<<16|sumToInt<<8|sumToInt;
                    newImage.setRGB(i,j,sumToInt);
                }
            }
        }
        return newImage;
    }
}




