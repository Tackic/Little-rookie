# 编写shell脚本，输出9×9表
## 要求
> 1.	在一页纸上打印代码及运行结果
> 2.  白底黑字
> 3.  乘法部分，必须使用函数完成（传递两个参数）
> 4. 第五行不输出
> 5. 输出表格的格式漂亮的话有+1

## 源代码
```
calc(){
	echo "$1*$2=$[ $1 * $2]"
}
for ((i=1; i < 10; i++)) {
	for ((j=1; j < 10; j++)) {
		if ((i == 5)) {
			break;
		}
		echo -n  `calc $i $j`'\t'
	}
	echo -n '\n'
}
```
## 运行结果
```
1*1=1	1*2=2	1*3=3	1*4=4	1*5=5	1*6=6	1*7=7	1*8=8	1*9=9
2*1=2	2*2=4	2*3=6	2*4=8	2*5=10	2*6=12	2*7=14	2*8=16	2*9=18
3*1=3	3*2=6	3*3=9	3*4=12	3*5=15	3*6=18	3*7=21	3*8=24	3*9=27
4*1=4	4*2=8	4*3=12	4*4=16	4*5=20	4*6=24	4*7=28	4*8=32	4*9=36
======================================================================
6*1=6	6*2=12	6*3=18	6*4=24	6*5=30	6*6=36	6*7=42	6*8=48	6*9=54
7*1=7	7*2=14	7*3=21	7*4=28	7*5=35	7*6=42	7*7=49	7*8=56	7*9=63
8*1=8	8*2=16	8*3=24	8*4=32	8*5=40	8*6=48	8*7=56	8*8=64	8*9=72
9*1=9	9*2=18	9*3=27	9*4=36	9*5=45	9*6=54	9*7=63	9*8=72	9*9=81
```