package util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import imgdo.ImgData;

public class ImgTextCut {
	private static final Color WHITE=new Color(255,255,255),BLACK=new Color(0,0,0);

	/*public void test() throws IOException{
		int count=0;
		List<List<BufferedImage>> lists=cutToImg("F:\\tmImg\\jpgs\\1.jpg");
		for(List<BufferedImage> list : lists){
			for(BufferedImage data:list){
				ImageIO.write(data, "jpg", new File("F:\\tmImg\\yb\\t\\"+count+".jpg"));
				count++;
			}
		}
	}
	/*
	public List<List<double[][]>> cutToArr(File f) throws IOException{
		double[][] arr=twoValueByInt(ImageIO.read(f), 127);
		return cutToArr(arr);
	}
	public List<List<BufferedImage>> cutToImg(File f) throws IOException{
		double[][] arr=twoValueByInt(ImageIO.read(f), 127);
		return listArrToImg(cutToArr(arr));
	}
	
	public List<List<BufferedImage>> cutToImg(String uri) throws IOException{
		double[][] arr=twoValueByInt(ImageIO.read(new File(uri)), 127);
		return listArrToImg(cutToArr(arr));
	}*/
	
	public List<ImgData> cutToArr(String uri) throws IOException{
		double[][] arr=twoValueByDouble(ImageIO.read(new File(uri)), 127);
		return cutToArr(arr);
	}
	public List<ImgData> cutToArr(double[][] arr){
		List<ImgData> re=new ArrayList<ImgData>();
		List<double[][]> reX=cutX(arr);
		for(int i=0,len=reX.size();i<len;i++){
			ImgData reY=cutY(reX.get(i));
			re.add(reY);
		}
		return re;
	}/*
	public List<List<BufferedImage>> cutToImg(double[][] arr){
		return listArrToImg(cutToArr(arr));
	}*/
	private List<double[][]> cutX(double[][] arr){
		int length=arr[0].length;
		List<double[][]> re=new ArrayList<double[][]>();
		int[] count =countX(arr);
		List<Integer> point =findPoint(count);
		//int lengthP=point.size();
		int lengthP=6;
		if(lengthP%2==1)
			lengthP=lengthP-1;
		for(int i=0;i<lengthP;i=i+2)
			re.add(cutArray(arr,point.get(i),point.get(i+1),0,length));
		return re;
	}
	private ImgData cutY(double[][] arr){
		int length=arr.length;
		ImgData re=new ImgData(),head=re;
		int[] count =countY(arr);
		List<Integer> point =findPoint(count);
		boolean isBegin=true;
		for(int i=0,len=point.size();i<len;i=i+2){
			//a:当前点的起始坐标  b:当前点的结束坐标  c:下一点的起始坐标（如果当前点是最后一个点，则c为当前点的结束坐标）
			int a=point.get(i),b=point.get(i+1),c=(i+2)<len?point.get(i+2):b;
			double cf=(b-a)/(length+0.0);//计算字的长宽比
			double cj=cf-(int)cf;//计算长宽比小数部分
			int zs=(int)cf;//计算长宽比整数部分
			int bs=0;//几个字
			if( cj>=0.8 && zs>=1){ //如果是 n.8<x<n+1.2 比如1.8<x<2.2则是两个字,因为一个字的长宽比约为1:1
				bs=zs+1;
				re=cutYAvg(arr,a,b,c,bs,re);
				isBegin=true;
			}else if( cj<0.2 && zs>=2){
				bs=zs;
				re=cutYAvg(arr,a,b,c,bs,re);
				isBegin=true;
			}else if( cj>=0.2 && cj<0.8 && zs>=1){
				bs=zs+1;
				int beforEnd=isBegin?0:point.get(i-1);
				re=cutYThan(arr,beforEnd,a,b,c,bs,isBegin,re);
				isBegin=!isBegin;
			}else{
				bs=1;
				re.data=cutArray(arr,0,length,a,b);
				re.img=arrToImg(re.data);
				re.nextSpace=c==b?0:c-b-1;
				re.nextImgData=new ImgData();
				re.nextImgData.beforeImgData=re;
				re=re.nextImgData;
				isBegin=true;
			}
		}
		return head;
	}
	/**
	 *通过计算平均值来切割连接字块
	 *字数据  字数据中起始点  终止点  字数  返回结果集
	 **/
	private ImgData cutYAvg(double[][]arr,int begin,int end,int nextBegin,int bs,ImgData re){
		int height=arr.length;
		int length=end-begin;
		int pj=length/bs; //总长度除以几个字得到一个字的长度
		for(int j=1;j<bs;j++){
			re.data=cutArray(arr, 0,height,begin+pj*(j-1),begin+pj*j);
			re.img=arrToImg(re.data);
			re.nextSpace=0;
			re.nextImgData=new ImgData();
			re.nextImgData.beforeImgData=re;
			re=re.nextImgData;
		}
		re.data=cutArray(arr,0,height,begin+pj*(bs-1),end);
		re.img=arrToImg(re.data);
		re.nextSpace=nextBegin==end?0:nextBegin-end-1;
		re.nextImgData=new ImgData();
		re.nextImgData.beforeImgData=re;
		re=re.nextImgData;
		return re;
	}
	/**
	 *通过字的固定比例来切割连接字块，如果长宽比为1.3，则分为两个字，一个为1，一个为0.3
	 *字数据  上一个结束点(用于连接分割开的字)  字数据中起始点  终止点  字数  多余部分保留在前面还是后面  返回结果集
	**/
	private ImgData cutYThan(double[][]arr,int beforEnd,int begin,int end,int nextBegin,int bs,boolean isBegin,ImgData re){
		int height=arr.length;
		if( isBegin){
			for(int j=1;j<bs;j++){
				re.data=cutArray(arr, 0,height,begin+height*(j-1),begin+height*j);
				re.img=arrToImg(re.data);
				re.nextSpace=0;
				re.nextImgData=new ImgData();
				re.nextImgData.beforeImgData=re;
				re=re.nextImgData;
			}
			re.data=cutArray(arr,0,height,begin+height*(bs-1),end);
			re.img=arrToImg(re.data);
			re.nextSpace=nextBegin==end?0:nextBegin-end-1;
			re.nextImgData=new ImgData();
			re.nextImgData.beforeImgData=re;
			re=re.nextImgData;
		}else{//从后向前
			List<double[][]> ree=new ArrayList<double[][]>();
			for(int j=1;j<bs;j++){
				ree.add(cutArray(arr, 0,height,end-height*j,end-height*(j-1)));
			}
			ree.add(cutArray(arr,0,height,begin,end-height*(bs-1)));
			re=re.beforeImgData;//找到最后一个数据（re总是指向末尾的，链表末尾是空数据，末尾的前一个是最后一个字数据）
			double[][]  beforZ=re.data;
			double[][]  afterZ=ree.get(ree.size()-1);
			//re.remove(re.size()-1);
			re.data=link(beforZ,afterZ,begin-beforEnd);
			re.img=arrToImg(re.data);
			re.nextSpace=0;
			re.nextImgData=new ImgData();
			re.nextImgData.beforeImgData=re;
			re=re.nextImgData;
			//re.add(link(beforZ,afterZ,begin-beforEnd));
			for(int j=ree.size()-2;j>-1;j--){//由于是从后向前，所以最后一个字是第一个，而第一个字与前一个字相结合，所以从第二个字开始存
				re.data=ree.get(j);
				re.img=arrToImg(re.data);
				re.nextSpace=j==0?nextBegin==end?0:nextBegin-end-1:0;
				re.nextImgData=new ImgData();
				re.nextImgData.beforeImgData=re;
				re=re.nextImgData;
			}
		}
		return re;
	}
	
	private double[][] cutArray(double[][] arr,int xB,int xE,int yB,int yE){
		int w=xE-xB,h=yE-yB;
		double[][] re=new double[w][h];
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				re[i][j]=arr[i+xB][j+yB];
			}
		}
		return re;
	}
	
	//连接两张图片  参数：前一张图  后一张图  二者中间空白的距离
	private double[][] link(double[][] befor,double[][] after,int length){
		//创建新图片 高度等于两张图片的最低高度，宽度等于二者宽度之和再加中间空白宽度
		int minH=Math.min(befor.length,after.length),lenB=befor[0].length,lenA=after[0].length;
		double[][] re=new double[minH][lenB+length+lenA];
		
		for(int i=0;i<minH;i++){
			for(int j=0;j<lenB;j++){
				re[i][j]=befor[i][j];
			}
			for(int j=0;j<length;j++){
				re[i][j+lenB]=0;//中间都用空白填充
			}
			for(int j=0;j<lenA;j++){
				re[i][j+lenB+length]=after[i][j];
			}
		}
		return re;
	}
	//统计一行黑色点的个数
	private int[] countX(double[][] arr){
		int[] count=new int[arr.length];
		for(int i=0,len=arr.length;i<len;i++){
			int count2=0;
			for(int j=0,jlen=arr[i].length;j<jlen;j++){
				if(arr[i][j]!=0){
					count2++;
				}
			}
			count[i]=count2;
		}
		return count;
	}
	//统计一列黑色点的个数
	private int[] countY(double[][] arr){
		int[] count=new int[arr[0].length];//列数
		for(int i=0,len=arr[0].length;i<len;i++){
			int count2=0;//每列的黑色点数
			for(int j=0,jlen=arr.length;j<jlen;j++){
				if(arr[j][i]!=0){//不为0则是黑色
					count2++;
				}
			}
			count[i]=count2;
		}
		return count;
	}
	//参数：纵向或横向的映射数组  返回：一维数组，0是开始1是结束，2是开始3是结束。表示行或列的分水岭  
	private List<Integer> findPoint(int[] arr){
		int index=-1;
		List<Integer> re=new ArrayList<Integer>();
		zeroToMore(arr,index,re);
		return re;
	}
	//参数：纵向或横向的映射数组 数组当前下标 返回结果集  返回：re，一维数组   递归调用，记录非零下标，表示一行的开始
	private void zeroToMore(int[] arr,int index,List<Integer> re){
		if(index<(arr.length-1)){//索引到达最后则终止递归
			for(int i=index+1,len=arr.length;i<len;i++){//从索引处向后扫描
				if(arr[i]>1){//如果黑色点数大于1，则记录下标转到下一判断
					index=i;
					re.add(i);
					break;
				}
				if(i==(len-1)){//如果扫描到最后都没有大于1的，则终止递归
					index=i;
				}
			}
			moreToZero(arr,index,re);
		}
	}
	//同上  记录第一个零下标，表示一行的结束
	private void moreToZero(int[] arr,int index,List<Integer> re){
		if(index<(arr.length-1)){
			for(int i=index+1,len=arr.length;i<len;i++){
				if(arr[i]<=1){
					index=i;
					re.add(i);
					break;
				}
				if(i==(len-1)){
					index=i;
				}
			}
			zeroToMore(arr,index,re);
		}
	}
	
	private List<List<BufferedImage>> listArrToImg(List<List<double[][]>> lists){
		List<List<BufferedImage>> re=new ArrayList<List<BufferedImage>>();
		for(List<double[][]> list : lists){
			List<BufferedImage> l=new ArrayList<BufferedImage>();
			for(double[][] data:list){
				l.add(arrToImg(data));
			}
			re.add(l);
		}
		return re;
	}
	private BufferedImage arrToImg(double[][] arr){
		int h=arr.length,w=arr[0].length;
		BufferedImage img=new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<h;i++){
			for(int j=0;j<w;j++){
				if(arr[i][j]!=0){
					img.setRGB(j, i, BLACK.getRGB());
				}else{
					img.setRGB(j, i, WHITE.getRGB());
				}
			}
		}
		return img;
	}
	
	/**
     * 二值化
     * flag:阀值   大于为0  小于为1
     */
	private double[][] twoValueByDouble(BufferedImage bimg,int flag){
		int wid=bimg.getWidth();
		int hei=bimg.getHeight();
		double[][] re=new double[hei][wid];
		for(int i=0;i<hei;i++){
			for(int j=0;j<wid;j++){
				Color c=new Color(bimg.getRGB(j, i));
				int avg=(c.getBlue()+c.getGreen()+c.getRed())/3;
				if(avg>flag){
					re[i][j]=0;
				}else{
					re[i][j]=1;
				}
				
			}
		}
		return re;
	}
}
