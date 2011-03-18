%% Updates from 2009/05/27, by Adrian Perez <aperez@igalia.com>
%%
%%  * Embedded logos by using TikZ/PGF figures: no need for external
%%    files, high-quality vector output of our logos. TikZ code was
%%    generated with the "inkscape2tikz" InkScape plugin, found here:
%%    http://code.google.com/p/inkscape2tikz/
%%
%% Updates from 2009/06/25, by Adrian Perez <aperez@igalia.com>
%%
%%  * Make this inherit the scrbook class instead of implementing a
%%    duplicated class by copy-pasting the stock "book" class and
%%    then modifying things.

\NeedsTeXFormat{LaTeX2e}[1995/12/01]
\ProvidesClass{igaliabk}
              [2009/06/27 LaTeX class for Igalia documentation]

%\DeclareOption*{\PassOptionsToClass{\CurrentOption}{scrbook}}

\LoadClass[11pt,oneside,onecolumn,a4paper,openany]{scrbook}

\PassOptionsToPackage{colorlinks=true,
	linkcolor=blue, urlcolor=blue,
	pdfstartview=FitH}{hyperref}
\PassOptionsToPackage{hyperref}{ucs}
\PassOptionsToPackage{T1}{fontenc}
\PassOptionsToPackage{english}{babel}

\RequirePackage{ucs}
\RequirePackage{lmodern}
\RequirePackage{fontenc}
\RequirePackage{hyperref}

\AtBeginDocument{\PreloadUnicodePage{0}}
\AtBeginDocument{\PreloadUnicodePage{1}}

\ifx\XeTeXversion\undefined
	\renewcommand\rmdefault{ppl}
	\relax
\else
	\RequirePackage{xunicode}
	\RequirePackage{xltxtra}
	\defaultfontfeatures{Mapping=tex-text}
	\setromanfont{Bitstream Charter}
	\setmonofont{monospace}
	\setsansfont{sans}
\fi

\ifx\pdftexversion\undefined
	\relax
\else
	\PassOptionsToPackage{pdftex}{graphicx}
\fi

\RequirePackage{ifthen}
\RequirePackage{graphicx}
\RequirePackage{eso-pic}
\RequirePackage{xspace}
\RequirePackage{color}
\RequirePackage{tikz}
\RequirePackage{eurosym}
%\RequirePackage{flafter}
%\RequirePackage{float}
%\floatplacement{figure}{H}

\RequirePackage{babel}


\newcommand{\customer}[1]%
	{\ifx\empty#1\empty\else\gdef\@customer{#1}\fi}
\customer{\@latex@warning@no@line{No customer given}}

\newcommand{\reference}[1]%
	{\ifx\empty#1\empty\else\gdef\@reference{#1}\fi}
\reference{\@latex@warning@no@line{No reference given}}

\newcommand{\city}[1]%
	{\ifx\empty#1\empty\else\gdef\@city{#1}\fi}
\city{\@latex@warning@no@line{No city given}}


\definecolor{c003082}{RGB}{0,48,130}
\definecolor{c0067b1}{RGB}{0,103,177}
\definecolor{c32012a}{RGB}{50,1,42}
\definecolor{c3f3f3f}{RGB}{63,63,63}
\definecolor{c3f3f3f}{RGB}{63,63,63}
\definecolor{c61a8cb}{RGB}{97,168,203}
\definecolor{c66accd}{RGB}{102,172,205}
\definecolor{c670164}{RGB}{103,1,100}
\definecolor{c6c9807}{RGB}{108,152,7}
\definecolor{ca1c22d}{RGB}{161,194,45}
\definecolor{cacd2e4}{RGB}{172,210,228}
\definecolor{cb30031}{RGB}{179,0,49}
\definecolor{cb40030}{RGB}{180,0,48}
\definecolor{cbfe7e5}{RGB}{191,231,229}
\definecolor{ccfe016}{RGB}{207,224,22}
\definecolor{cd4e314}{RGB}{212,227,20}
\definecolor{ceef69b}{RGB}{238,246,155}
\definecolor{cf0f79a}{RGB}{240,247,154}
\definecolor{cff0000}{RGB}{255,0,0}
\definecolor{cff6300}{RGB}{255,99,0}
\definecolor{cff91af}{RGB}{255,145,175}
\definecolor{cff9900}{RGB}{255,153,0}
\definecolor{cffc440}{RGB}{255,196,64}
\definecolor{cffc744}{RGB}{255,199,68}
\definecolor{cffdd91}{RGB}{255,221,145}
\definecolor{cffde91}{RGB}{255,222,145}
\definecolor{cffff00}{RGB}{255,255,0}
\definecolor{cffff99}{RGB}{255,255,153}
\definecolor{cf9fbbc}{RGB}{249,251,188}
\definecolor{cebf2c3}{RGB}{235,242,195}
\definecolor{cf5c6c8}{RGB}{245,198,200}
\definecolor{cdfc6d6}{RGB}{223,198,214}
\definecolor{cc7dbec}{RGB}{199,219,236}
\definecolor{cedf7f7}{RGB}{237,247,247}
\definecolor{cffe4bb}{RGB}{255,228,187}
\definecolor{cfff9da}{RGB}{255,249,218}
\definecolor{cbccadf}{RGB}{188,202,223}
\definecolor{cffd5ba}{RGB}{255,213,186}

\newsavebox{\igaliaMiniLogo}
\sbox{\igaliaMiniLogo}{
\scalebox{0.25}{
	\begin{tikzpicture}[y=0.80pt,x=0.80pt,yscale=-1]
	%letter-i1
	\path[fill=c3f3f3f] (233.5810,47.8746) .. controls (233.5810,55.3506) and
	(231.4230,56.6176) .. (224.5890,56.6176) .. controls (217.6170,56.6176) and
	(215.4660,55.3496) .. (215.4660,47.8746) .. controls (215.4660,41.1606) and
	(217.6170,40.1516) .. (224.5890,40.1516) .. controls (231.4230,40.1516) and
	(233.5810,41.1606) .. (233.5810,47.8746) -- (233.5810,47.8746) --
	cycle(232.3110,113.3446) .. controls (232.3110,123.3206) and
	(237.1200,128.5636) .. (237.1200,128.5636) .. controls (237.1200,128.5636) and
	(230.7370,133.6006) .. (225.8170,133.6006) .. controls (219.3530,133.6006) and
	(216.6020,121.9866) .. (216.6020,117.1836) .. controls (216.6020,95.5006) and
	(216.6020,67.0016) .. (216.6020,67.0016) -- (232.3110,67.0016) .. controls
	(232.3110,67.0016) and (232.3110,103.3796) .. (232.3110,113.3446) -- cycle;
	%letter-i2
	\path[fill=c3f3f3f] (420.9640,47.8746) .. controls (420.9640,55.3506) and
	(418.8060,56.6176) .. (411.9660,56.6176) .. controls (404.9970,56.6176) and
	(402.8450,55.3496) .. (402.8450,47.8746) .. controls (402.8450,41.1606) and
	(404.9970,40.1516) .. (411.9660,40.1516) .. controls (418.8060,40.1516) and
	(420.9640,41.1606) .. (420.9640,47.8746) -- (420.9640,47.8746) --
	cycle(419.6930,113.3446) .. controls (419.6930,123.3206) and
	(424.5000,128.5636) .. (424.5000,128.5636) .. controls (424.5000,128.5636) and
	(418.1170,133.6006) .. (413.1990,133.6006) .. controls (406.7360,133.6006) and
	(403.9840,121.9866) .. (403.9840,117.1836) .. controls (403.9840,95.5006) and
	(403.9840,67.0016) .. (403.9840,67.0016) -- (419.6930,67.0016) .. controls
	(419.6930,67.0016) and (419.6930,103.3796) .. (419.6930,113.3446) --
	(419.6930,113.3446) -- cycle;
	%letter-g
	\path[fill=c3f3f3f] (268.2870,133.0026) .. controls (250.5530,133.0026) and
	(241.1760,125.6546) .. (241.1760,99.4296) .. controls (241.1760,73.0836) and
	(250.9310,65.1036) .. (272.2130,65.1036) .. controls (279.0590,65.1036) and
	(287.3710,66.0956) .. (294.0350,67.8666) .. controls (300.8920,69.6896) and
	(300.3340,75.3706) .. (300.3340,75.3706) -- (300.3340,126.5386) .. controls
	(300.3340,151.7456) and (286.2750,157.3266) .. (265.8830,157.3266) .. controls
	(260.9320,157.3266) and (254.8650,156.6576) .. (249.7680,155.4256) .. controls
	(243.1100,153.8186) and (245.5100,144.2846) .. (245.5100,144.2846) --
	(246.3690,140.8566) .. controls (252.3240,142.7556) and (259.6700,144.0226) ..
	(265.7520,144.0226) .. controls (277.6620,144.0226) and (284.6270,140.7256) ..
	(284.6270,128.8206) .. controls (281.0820,130.9706) and (275.6340,133.0026) ..
	(268.2870,133.0026) -- (268.2870,133.0026) -- cycle(284.6260,83.1536) ..
	controls (284.6260,83.1536) and (284.9680,79.9466) .. (280.7820,79.0326) ..
	controls (277.9340,78.4116) and (274.8700,78.0236) .. (272.2120,78.0236) ..
	controls (261.0670,78.0236) and (257.3940,80.9376) .. (257.3940,99.3036) ..
	controls (257.3940,115.3896) and (260.8100,119.4476) .. (271.8320,119.4476) ..
	controls (278.7970,119.4476) and (284.6270,117.2896) .. (284.6270,113.3666) --
	(284.6270,83.1536) -- (284.6260,83.1536) -- cycle;
	%letter-l
	\path[fill=c3f3f3f] (391.9170,49.7006) -- (391.9170,113.3666) .. controls
	(391.9170,119.5716) and (393.3130,123.8806) .. (396.8620,128.5686) .. controls
	(396.8620,128.5686) and (384.7550,138.4986) .. (380.1140,130.4896) .. controls
	(377.6140,126.1776) and (376.2100,121.0266) .. (376.2100,113.9996) --
	(376.2100,40.0206) .. controls (376.2100,40.0206) and (378.4230,40.0206) ..
	(382.6920,40.0206) .. controls (392.6260,40.0206) and (391.9170,49.7006) ..
	(391.9170,49.7006) -- (391.9170,49.7006) -- cycle;
	%letter-a2
	\path[fill=c3f3f3f] (484.5150,122.6586) .. controls (484.5150,122.6586) and
	(484.5150,128.3566) .. (477.9450,130.2746) .. controls (470.7440,132.3736) and
	(462.1680,133.2506) .. (455.3810,133.2506) .. controls (434.9900,133.2506) and
	(428.1430,128.5686) .. (428.1430,111.7186) .. controls (428.1430,93.4766) and
	(438.4070,90.5646) .. (453.4830,90.5646) .. controls (458.9320,90.5646) and
	(463.9970,91.1926) .. (469.4440,92.3346) .. controls (469.4440,81.0666) and
	(466.7840,78.2786) .. (453.3560,78.2786) .. controls (449.5980,78.2786) and
	(445.8380,78.6076) .. (442.3560,79.2476) .. controls (433.2490,80.9096) and
	(432.2470,74.3596) .. (432.2470,74.3596) -- (430.9350,68.5236) .. controls
	(434.4350,67.5996) and (438.7490,66.7106) .. (443.3940,66.0666) .. controls
	(447.6670,65.4796) and (452.2180,65.1036) .. (456.6540,65.1036) .. controls
	(475.7790,65.1036) and (484.5170,70.9296) .. (484.5170,90.1856) --
	(484.5170,122.6586) -- (484.5150,122.6586) -- cycle(469.4430,115.9826) --
	(469.4430,102.9766) .. controls (465.8980,102.2146) and (461.3360,101.8366) ..
	(456.6520,101.8366) .. controls (447.7830,101.8366) and (444.6130,102.8526) ..
	(444.6130,111.0846) .. controls (444.6130,119.8236) and (447.0190,121.2196) ..
	(456.5190,121.2196) .. controls (459.8220,121.2196) and (463.1950,120.7526) ..
	(466.1990,120.0176) .. controls (469.5310,119.2256) and (469.4430,115.9826) ..
	(469.4430,115.9826) -- (469.4430,115.9826) -- cycle;
	%letter-a1
	\path[fill=c3f3f3f] (365.4430,122.6586) .. controls (365.4430,122.6586) and
	(365.4470,128.3566) .. (358.8710,130.2746) .. controls (351.6760,132.3736) and
	(343.0980,133.2506) .. (336.3070,133.2506) .. controls (315.9160,133.2506) and
	(309.0780,128.5686) .. (309.0780,111.7186) .. controls (309.0780,93.4766) and
	(319.3320,90.5646) .. (334.4100,90.5646) .. controls (339.8570,90.5646) and
	(344.9240,91.1926) .. (350.3710,92.3346) .. controls (350.3710,81.0666) and
	(347.7130,78.2786) .. (334.2830,78.2786) .. controls (330.5310,78.2786) and
	(326.7710,78.6076) .. (323.2830,79.2476) .. controls (314.1760,80.9096) and
	(313.1780,74.3596) .. (313.1780,74.3596) -- (311.8600,68.5236) .. controls
	(315.3560,67.5996) and (319.6740,66.7106) .. (324.3230,66.0666) .. controls
	(328.5940,65.4796) and (333.1450,65.1036) .. (337.5770,65.1036) .. controls
	(356.7060,65.1036) and (365.4440,70.9296) .. (365.4440,90.1856) --
	(365.4440,122.6586) -- (365.4430,122.6586) -- cycle(350.3710,115.9826) --
	(350.3710,102.9766) .. controls (346.8220,102.2146) and (342.2640,101.8366) ..
	(337.5760,101.8366) .. controls (328.7110,101.8366) and (325.5430,102.8526) ..
	(325.5430,111.0846) .. controls (325.5430,119.8236) and (327.9470,121.2196) ..
	(337.4490,121.2196) .. controls (340.7520,121.2196) and (344.1250,120.7526) ..
	(347.1270,120.0176) .. controls (350.4600,119.2256) and (350.3710,115.9826) ..
	(350.3710,115.9826) -- (350.3710,115.9826) -- cycle;
	%green-bottom
	\shade[top color=cffff00,bottom color=ca1c22d]
	(25.1250,25.2769) .. controls (24.3240,26.0779) and
	(23.5360,26.9188) .. (22.7500,27.7769) .. controls (22.7097,27.8208) and
	(22.6652,27.8577) .. (22.6250,27.9019) .. controls (21.8646,28.7342) and
	(21.1226,29.6067) .. (20.3750,30.4956) .. controls (19.5908,31.4321) and
	(18.8222,32.4085) .. (18.0625,33.4019) .. controls (17.2988,34.4003) and
	(16.5157,35.4099) .. (15.7812,36.4644) .. controls (15.3477,37.0868) and
	(14.9221,37.7291) .. (14.5000,38.3706) .. controls (14.2006,38.8216) and
	(13.8876,39.2534) .. (13.5938,39.7144) .. controls (13.4959,39.8694) and
	(13.4097,40.0270) .. (13.3125,40.1831) .. controls (6.2177,51.4652) and
	(0.8818,65.6729) .. (0.1250,82.0894) .. controls (0.0985,82.6631) and
	(0.0776,83.2289) .. (0.0625,83.8081) .. controls (0.0245,84.8782) and
	(0.0000,85.9710) .. (0.0000,87.0581) .. controls (0.0000,108.5808) and
	(7.9542,126.2786) .. (17.8125,139.1206) .. controls (18.5895,140.1328) and
	(19.3896,141.1386) .. (20.1875,142.0894) .. controls (20.8152,142.8374) and
	(21.4251,143.5674) .. (22.0625,144.2769) .. controls (22.2479,144.4837) and
	(22.4386,144.6980) .. (22.6250,144.9019) .. controls (23.1501,145.4748) and
	(23.6895,146.0116) .. (24.2188,146.5581) .. controls (24.5247,146.8743) and
	(24.8170,147.2188) .. (25.1250,147.5269) .. controls (35.5050,137.1459) and
	(51.5285,121.3099) .. (61.0625,111.7769) .. controls (54.7489,105.4629) and
	(46.7770,96.4657) .. (39.5000,86.4956) .. controls (46.7710,76.5936) and
	(54.7272,67.6715) .. (61.0312,61.3706) .. controls (51.4982,51.8336) and
	(35.5040,35.6549) .. (25.1250,25.2769) -- cycle;
	%red-bottom
	\shade[left color=cff0000,right color=c670164]
	(111.4688,111.7456) .. controls (105.1609,118.0515) and
	(96.1840,125.9953) .. (86.2500,133.2456) .. controls (76.3170,125.9939) and
	(67.3686,118.0854) .. (61.0625,111.7769) .. controls (51.5285,121.3099) and
	(35.5060,137.1459) .. (25.1250,147.5269) .. controls (26.7398,149.1410) and
	(28.5045,150.7280) .. (30.3438,152.2769) .. controls (31.2872,153.0714) and
	(32.2493,153.8502) .. (33.2500,154.6206) .. controls (33.9418,155.1532) and
	(34.6564,155.6640) .. (35.3750,156.1831) .. controls (35.6947,156.4158) and
	(35.9872,156.6717) .. (36.3125,156.9019) .. controls (36.4367,156.9891) and
	(36.5625,157.0651) .. (36.6875,157.1519) .. controls (37.7688,157.9088) and
	(38.8897,158.6461) .. (40.0312,159.3706) .. controls (51.7151,166.7861) and
	(66.5009,172.3399) .. (83.7500,172.7456) .. controls (84.7399,172.7785) and
	(85.7451,172.8081) .. (86.7500,172.8081) .. controls (108.2761,172.8073) and
	(126.0871,164.7405) .. (138.9688,154.8394) .. controls (139.9834,154.0595) and
	(140.9851,153.2642) .. (141.9375,152.4644) .. controls (142.6872,151.8347) and
	(143.4146,151.2280) .. (144.1250,150.5894) .. controls (144.3330,150.4023) and
	(144.5454,150.2145) .. (144.7500,150.0269) .. controls (144.7828,149.9969) and
	(144.8110,149.9632) .. (144.8437,149.9332) .. controls (145.3857,149.4347) and
	(145.8884,148.9037) .. (146.4062,148.4019) .. controls (146.7207,148.0977) and
	(147.0687,147.8017) .. (147.3750,147.4957) .. controls (137.0000,137.1146) and
	(120.9958,121.2746) .. (111.4688,111.7456) -- cycle;
	%blue-bottom
	\shade[left color=c0067b1,right color=cbfe7e5]
	(85.7500,-0.0044) .. controls (64.2191,-0.0044) and
	(46.3833,8.0630) .. (33.5000,17.9644) .. controls (32.5571,18.6890) and
	(31.6392,19.4098) .. (30.7500,20.1519) .. controls (30.6537,20.2318) and
	(30.5644,20.3217) .. (30.4688,20.4019) .. controls (30.1195,20.6958) and
	(29.7782,20.9808) .. (29.4375,21.2769) .. controls (28.8610,21.7760) and
	(28.2725,22.2703) .. (27.7188,22.7769) .. controls (27.1473,23.3006) and
	(26.6385,23.8432) .. (26.0938,24.3706) .. controls (25.7754,24.6787) and
	(25.4349,24.9669) .. (25.1250,25.2769) .. controls (35.5030,35.6519) and
	(51.5335,51.8114) .. (61.0625,61.3394) .. controls (67.4026,54.9979) and
	(76.4116,46.9766) .. (86.4062,39.6519) .. controls (96.2752,46.8705) and
	(105.1647,54.7534) .. (111.4375,61.0269) .. controls (120.9615,51.4939) and
	(136.9677,35.6266) .. (147.3438,25.2456) .. controls (145.7210,23.6223) and
	(143.9751,22.0526) .. (142.1250,20.4956) .. controls (141.1973,19.7149) and
	(140.2330,18.9405) .. (139.2500,18.1831) .. controls (139.1461,18.1026) and
	(139.0421,18.0134) .. (138.9375,17.9331) .. controls (138.0439,17.2513) and
	(137.1252,16.5607) .. (136.1875,15.9019) .. controls (136.0549,15.8080) and
	(135.9148,15.7140) .. (135.7812,15.6206) .. controls (135.5784,15.4798) and
	(135.3611,15.3540) .. (135.1562,15.2144) .. controls (134.2785,14.6110) and
	(133.3853,14.0151) .. (132.4688,13.4331) .. controls (121.2301,6.2962) and
	(107.1014,0.8829) .. (90.6875,0.1206) .. controls (90.1934,0.0934) and
	(89.6855,0.0772) .. (89.1875,0.0581) .. controls (89.1146,0.0560) and
	(89.0417,0.0600) .. (88.9688,0.0581) .. controls (87.8991,0.0196) and
	(86.8372,-0.0044) .. (85.7500,-0.0044) -- cycle;
	%yellow-bottom
	\shade[top color=cff9900,bottom color=cffff99]
	(147.3438,25.2769) .. controls (136.9677,35.6579) and
	(120.9958,51.4646) .. (111.4688,60.9956) .. controls (117.8080,67.3355) and
	(125.7926,76.3824) .. (133.0938,86.4019) .. controls (125.7991,96.4065) and
	(117.8025,105.4128) .. (111.4688,111.7456) .. controls (121.0048,121.2836) and
	(137.0000,137.1146) .. (147.3750,147.4956) .. controls (148.9992,145.8706) and
	(150.5672,144.1288) .. (152.1250,142.2769) .. controls (152.9106,141.3430) and
	(153.7068,140.3916) .. (154.4688,139.4019) .. controls (154.8855,138.8604) and
	(155.3101,138.3035) .. (155.7188,137.7456) .. controls (156.1562,137.1529) and
	(156.5714,136.5455) .. (157.0000,135.9331) .. controls (157.7523,134.8583) and
	(158.4985,133.7549) .. (159.2188,132.6206) .. controls (166.6346,120.9411) and
	(172.1835,106.1387) .. (172.5938,88.9019) .. controls (172.6267,87.9122) and
	(172.6562,86.9065) .. (172.6562,85.9019) .. controls (172.6562,66.0829) and
	(165.8091,49.3886) .. (157.0000,36.8081) .. controls (156.9136,36.6837) and
	(156.8368,36.5567) .. (156.7500,36.4331) .. controls (156.0718,35.4748) and
	(155.3864,34.5612) .. (154.6875,33.6519) .. controls (153.9623,32.7083) and
	(153.2426,31.7917) .. (152.5000,30.9019) .. controls (152.4390,30.8288) and
	(152.3736,30.7558) .. (152.3125,30.6831) .. controls (152.2599,30.6202) and
	(152.2089,30.5582) .. (152.1562,30.4956) .. controls (151.3928,29.5920) and
	(150.6196,28.7165) .. (149.8438,27.8706) .. controls (149.8183,27.8428) and
	(149.8067,27.8046) .. (149.7812,27.7769) .. controls (149.2845,27.2367) and
	(148.7500,26.7618) .. (148.2500,26.2456) .. controls (147.9417,25.9271) and
	(147.6539,25.5870) .. (147.3438,25.2769) -- (147.3438,25.2769) -- cycle;
	%blue-top
	\shade[left color=c003082,right color=cbfe7e5]
	(86.7188,0.0269) .. controls (65.0396,0.0269) and
	(46.7961,7.8182) .. (33.5000,17.9644) .. controls (32.5547,18.6857) and
	(31.6449,19.4086) .. (30.7500,20.1519) .. controls (30.6537,20.2318) and
	(30.5644,20.3217) .. (30.4688,20.4019) .. controls (30.1195,20.6958) and
	(29.7782,20.9808) .. (29.4375,21.2769) .. controls (28.8610,21.7760) and
	(28.2725,22.2703) .. (27.7188,22.7769) .. controls (27.1473,23.3006) and
	(26.6385,23.8432) .. (26.0938,24.3706) .. controls (40.1613,12.0068) and
	(65.4731,24.3402) .. (86.4062,39.6519) .. controls (107.2671,24.3635) and
	(132.3808,12.1038) .. (146.3750,24.4019) .. controls (145.0285,23.0977) and
	(143.6332,21.7702) .. (142.1250,20.4956) .. controls (141.1927,19.7077) and
	(140.2425,18.9521) .. (139.2500,18.1831) .. controls (139.1461,18.1026) and
	(139.0421,18.0134) .. (138.9375,17.9331) .. controls (138.0439,17.2513) and
	(137.1252,16.5607) .. (136.1875,15.9019) .. controls (136.0549,15.8080) and
	(135.9148,15.7140) .. (135.7812,15.6206) .. controls (135.5784,15.4798) and
	(135.3611,15.3540) .. (135.1562,15.2144) .. controls (134.2700,14.6103) and
	(133.3921,14.0136) .. (132.4688,13.4331) .. controls (121.1009,6.2864) and
	(106.9492,1.0175) .. (90.6875,0.1206) .. controls (90.1934,0.0934) and
	(89.6855,0.0772) .. (89.1875,0.0581) .. controls (89.1146,0.0560) and
	(89.0417,0.0600) .. (88.9688,0.0581) .. controls (88.2180,0.0383) and
	(87.4787,0.0269) .. (86.7188,0.0269) -- (86.7188,0.0269) -- cycle;
	%green-top
	\shade[top color=cffff00,bottom color=c6c9807]
	(24.2500,26.2456) .. controls (23.7553,26.7569) and
	(23.2411,27.2415) .. (22.7500,27.7769) .. controls (22.7097,27.8208) and
	(22.6652,27.8577) .. (22.6250,27.9019) .. controls (21.8646,28.7342) and
	(21.1226,29.6067) .. (20.3750,30.4956) .. controls (19.5890,31.4302) and
	(18.8288,32.4061) .. (18.0625,33.4019) .. controls (17.2962,34.3976) and
	(16.5223,35.4088) .. (15.7812,36.4644) .. controls (15.3468,37.0832) and
	(14.9243,37.7315) .. (14.5000,38.3706) .. controls (14.2006,38.8216) and
	(13.8876,39.2534) .. (13.5938,39.7144) .. controls (13.4959,39.8694) and
	(13.4097,40.0270) .. (13.3125,40.1831) .. controls (6.2035,51.5988) and
	(1.0113,65.8170) .. (0.1250,82.0894) .. controls (0.0985,82.6631) and
	(0.0776,83.2289) .. (0.0625,83.8081) .. controls (0.0429,84.5600) and
	(0.0312,85.3285) .. (0.0312,86.0894) .. controls (0.0312,107.7661) and
	(7.7090,125.8648) .. (17.8125,139.1206) .. controls (18.5897,140.1403) and
	(19.3840,141.1278) .. (20.1875,142.0894) .. controls (20.8095,142.8337) and
	(21.4273,143.5680) .. (22.0625,144.2769) .. controls (22.2479,144.4837) and
	(22.4386,144.6980) .. (22.6250,144.9019) .. controls (23.1501,145.4748) and
	(23.6895,146.0116) .. (24.2188,146.5581) .. controls (11.8828,132.5261) and
	(24.1864,107.3503) .. (39.5000,86.4956) .. controls (24.2137,65.5522) and
	(11.9238,40.2709) .. (24.2500,26.2456) -- cycle;
	%red-top
	\shade[left color=cff0000,right color=c32012a]
	(86.2500,133.2456) .. controls (65.3492,148.5000) and
	(40.1246,160.7315) .. (26.0938,148.4019) .. controls (27.4418,149.7075) and
	(28.8333,151.0009) .. (30.3438,152.2769) .. controls (31.2760,153.0644) and
	(32.2575,153.8519) .. (33.2500,154.6206) .. controls (33.9355,155.1515) and
	(34.6612,155.6635) .. (35.3750,156.1831) .. controls (35.6947,156.4158) and
	(35.9872,156.6717) .. (36.3125,156.9019) .. controls (36.4367,156.9891) and
	(36.5625,157.0651) .. (36.6875,157.1519) .. controls (37.7682,157.9020) and
	(38.8942,158.6558) .. (40.0312,159.3706) .. controls (51.8532,166.8028) and
	(66.6702,172.1773) .. (83.7500,172.7456) .. controls (84.4201,172.7614) and
	(85.1038,172.7768) .. (85.7812,172.7768) .. controls (107.4579,172.7768) and
	(125.6702,164.9815) .. (138.9688,154.8393) .. controls (139.9917,154.0591) and
	(140.9735,153.2698) .. (141.9375,152.4643) .. controls (142.6837,151.8408) and
	(143.4148,151.2254) .. (144.1250,150.5893) .. controls (144.3330,150.4023) and
	(144.5454,150.2144) .. (144.7500,150.0268) .. controls (144.7828,149.9968) and
	(144.8110,149.9631) .. (144.8437,149.9331) .. controls (145.3857,149.4346) and
	(145.8884,148.9036) .. (146.4062,148.4019) .. controls (132.3793,160.7377) and
	(107.1510,148.5047) .. (86.2500,133.2456) -- cycle;
	%yellow-top
	\shade[top color=cff6300,bottom color=cffff99]
	(148.2500,26.2456) .. controls (160.5323,40.2559) and
	(148.3422,65.4886) .. (133.0938,86.4019) .. controls (148.3381,107.3217) and
	(160.5533,132.5271) .. (148.2500,146.5269) .. controls (149.5544,145.1799) and
	(150.8502,143.7855) .. (152.1250,142.2769) .. controls (152.9130,141.3443) and
	(153.6996,140.3946) .. (154.4688,139.4019) .. controls (154.8867,138.8624) and
	(155.3077,138.3025) .. (155.7188,137.7456) .. controls (156.1536,137.1519) and
	(156.5752,136.5451) .. (157.0000,135.9331) .. controls (157.7502,134.8523) and
	(158.5038,133.7577) .. (159.2188,132.6206) .. controls (166.6518,120.7981) and
	(172.0254,105.9774) .. (172.5938,88.9019) .. controls (172.6099,88.2234) and
	(172.6250,87.5566) .. (172.6250,86.8706) .. controls (172.6250,66.8606) and
	(166.0051,49.7772) .. (157.0000,36.8081) .. controls (156.9135,36.6837) and
	(156.8367,36.5567) .. (156.7500,36.4331) .. controls (156.0717,35.4748) and
	(155.3863,34.5612) .. (154.6875,33.6519) .. controls (153.9657,32.7062) and
	(153.2436,31.7972) .. (152.5000,30.9019) .. controls (152.4390,30.8288) and
	(152.3735,30.7558) .. (152.3125,30.6831) .. controls (152.2599,30.6202) and
	(152.2089,30.5582) .. (152.1562,30.4956) .. controls (151.3927,29.5920) and
	(150.6195,28.7165) .. (149.8437,27.8706) .. controls (149.8182,27.8428) and
	(149.8067,27.8046) .. (149.7812,27.7769) .. controls (149.2845,27.2367) and
	(148.7499,26.7618) .. (148.2500,26.2456) -- (148.2500,26.2456) -- cycle;
	\end{tikzpicture}
}}


\newsavebox{\igaliaShipoutLogo}
\sbox{\igaliaShipoutLogo}{
\scalebox{5.2}{
\begin{tikzpicture}[y=0.80pt,x=0.80pt,yscale=-1]
	%green-bottom
	\shade[top color=cf9fbbc,bottom color=cebf2c3]
	(25.1250,25.2769) .. controls (24.3240,26.0779) and
	(23.5360,26.9188) .. (22.7500,27.7769) .. controls (22.7097,27.8208) and
	(22.6652,27.8577) .. (22.6250,27.9019) .. controls (21.8646,28.7342) and
	(21.1226,29.6067) .. (20.3750,30.4956) .. controls (19.5908,31.4321) and
	(18.8222,32.4085) .. (18.0625,33.4019) .. controls (17.2988,34.4003) and
	(16.5157,35.4099) .. (15.7812,36.4644) .. controls (15.3477,37.0868) and
	(14.9221,37.7291) .. (14.5000,38.3706) .. controls (14.2006,38.8216) and
	(13.8876,39.2534) .. (13.5938,39.7144) .. controls (13.4959,39.8694) and
	(13.4097,40.0270) .. (13.3125,40.1831) .. controls (6.2177,51.4652) and
	(0.8818,65.6729) .. (0.1250,82.0894) .. controls (0.0985,82.6631) and
	(0.0776,83.2289) .. (0.0625,83.8081) .. controls (0.0245,84.8782) and
	(0.0000,85.9710) .. (0.0000,87.0581) .. controls (0.0000,108.5808) and
	(7.9542,126.2786) .. (17.8125,139.1206) .. controls (18.5895,140.1328) and
	(19.3896,141.1386) .. (20.1875,142.0894) .. controls (20.8152,142.8374) and
	(21.4251,143.5674) .. (22.0625,144.2769) .. controls (22.2479,144.4837) and
	(22.4386,144.6980) .. (22.6250,144.9019) .. controls (23.1501,145.4748) and
	(23.6895,146.0116) .. (24.2188,146.5581) .. controls (24.5247,146.8743) and
	(24.8170,147.2188) .. (25.1250,147.5269) .. controls (35.5050,137.1459) and
	(51.5285,121.3099) .. (61.0625,111.7769) .. controls (54.7489,105.4629) and
	(46.7770,96.4657) .. (39.5000,86.4956) .. controls (46.7710,76.5936) and
	(54.7272,67.6715) .. (61.0312,61.3706) .. controls (51.4982,51.8336) and
	(35.5040,35.6549) .. (25.1250,25.2769) -- cycle;
	%red-bottom
	\shade[left color=cf5c6c8,right color=cdfc6d6]
	(111.4688,111.7456) .. controls (105.1609,118.0515) and
	(96.1840,125.9953) .. (86.2500,133.2456) .. controls (76.3170,125.9939) and
	(67.3686,118.0854) .. (61.0625,111.7769) .. controls (51.5285,121.3099) and
	(35.5060,137.1459) .. (25.1250,147.5269) .. controls (26.7398,149.1410) and
	(28.5045,150.7280) .. (30.3438,152.2769) .. controls (31.2872,153.0714) and
	(32.2493,153.8502) .. (33.2500,154.6206) .. controls (33.9418,155.1532) and
	(34.6564,155.6640) .. (35.3750,156.1831) .. controls (35.6947,156.4158) and
	(35.9872,156.6717) .. (36.3125,156.9019) .. controls (36.4367,156.9891) and
	(36.5625,157.0651) .. (36.6875,157.1519) .. controls (37.7688,157.9088) and
	(38.8897,158.6461) .. (40.0312,159.3706) .. controls (51.7151,166.7861) and
	(66.5009,172.3399) .. (83.7500,172.7456) .. controls (84.7399,172.7785) and
	(85.7451,172.8081) .. (86.7500,172.8081) .. controls (108.2761,172.8073) and
	(126.0871,164.7405) .. (138.9688,154.8394) .. controls (139.9834,154.0595) and
	(140.9851,153.2642) .. (141.9375,152.4644) .. controls (142.6872,151.8347) and
	(143.4146,151.2280) .. (144.1250,150.5894) .. controls (144.3330,150.4023) and
	(144.5454,150.2145) .. (144.7500,150.0269) .. controls (144.7828,149.9969) and
	(144.8110,149.9632) .. (144.8437,149.9332) .. controls (145.3857,149.4347) and
	(145.8884,148.9037) .. (146.4062,148.4019) .. controls (146.7207,148.0977) and
	(147.0687,147.8017) .. (147.3750,147.4957) .. controls (137.0000,137.1146) and
	(120.9958,121.2746) .. (111.4688,111.7456) -- cycle;
	%blue-bottom
	\shade[left color=cc7dbec,right color=cedf7f7]
	(85.7500,-0.0044) .. controls (64.2191,-0.0044) and
	(46.3833,8.0630) .. (33.5000,17.9644) .. controls (32.5571,18.6890) and
	(31.6392,19.4098) .. (30.7500,20.1519) .. controls (30.6537,20.2318) and
	(30.5644,20.3217) .. (30.4688,20.4019) .. controls (30.1195,20.6958) and
	(29.7782,20.9808) .. (29.4375,21.2769) .. controls (28.8610,21.7760) and
	(28.2725,22.2703) .. (27.7188,22.7769) .. controls (27.1473,23.3006) and
	(26.6385,23.8432) .. (26.0938,24.3706) .. controls (25.7754,24.6787) and
	(25.4349,24.9669) .. (25.1250,25.2769) .. controls (35.5030,35.6519) and
	(51.5335,51.8114) .. (61.0625,61.3394) .. controls (67.4026,54.9979) and
	(76.4116,46.9766) .. (86.4062,39.6519) .. controls (96.2752,46.8705) and
	(105.1647,54.7534) .. (111.4375,61.0269) .. controls (120.9615,51.4939) and
	(136.9677,35.6266) .. (147.3438,25.2456) .. controls (145.7210,23.6223) and
	(143.9751,22.0526) .. (142.1250,20.4956) .. controls (141.1973,19.7149) and
	(140.2330,18.9405) .. (139.2500,18.1831) .. controls (139.1461,18.1026) and
	(139.0421,18.0134) .. (138.9375,17.9331) .. controls (138.0439,17.2513) and
	(137.1252,16.5607) .. (136.1875,15.9019) .. controls (136.0549,15.8080) and
	(135.9148,15.7140) .. (135.7812,15.6206) .. controls (135.5784,15.4798) and
	(135.3611,15.3540) .. (135.1562,15.2144) .. controls (134.2785,14.6110) and
	(133.3853,14.0151) .. (132.4688,13.4331) .. controls (121.2301,6.2962) and
	(107.1014,0.8829) .. (90.6875,0.1206) .. controls (90.1934,0.0934) and
	(89.6855,0.0772) .. (89.1875,0.0581) .. controls (89.1146,0.0560) and
	(89.0417,0.0600) .. (88.9688,0.0581) .. controls (87.8991,0.0196) and
	(86.8372,-0.0044) .. (85.7500,-0.0044) -- cycle;
	%yellow-bottom
	\shade[top color=cffe4bb,bottom color=cfff9da]
	(147.3438,25.2769) .. controls (136.9677,35.6579) and
	(120.9958,51.4646) .. (111.4688,60.9956) .. controls (117.8080,67.3355) and
	(125.7926,76.3824) .. (133.0938,86.4019) .. controls (125.7991,96.4065) and
	(117.8025,105.4128) .. (111.4688,111.7456) .. controls (121.0048,121.2836) and
	(137.0000,137.1146) .. (147.3750,147.4956) .. controls (148.9992,145.8706) and
	(150.5672,144.1288) .. (152.1250,142.2769) .. controls (152.9106,141.3430) and
	(153.7068,140.3916) .. (154.4688,139.4019) .. controls (154.8855,138.8604) and
	(155.3101,138.3035) .. (155.7188,137.7456) .. controls (156.1562,137.1529) and
	(156.5714,136.5455) .. (157.0000,135.9331) .. controls (157.7523,134.8583) and
	(158.4985,133.7549) .. (159.2188,132.6206) .. controls (166.6346,120.9411) and
	(172.1835,106.1387) .. (172.5938,88.9019) .. controls (172.6267,87.9122) and
	(172.6562,86.9065) .. (172.6562,85.9019) .. controls (172.6562,66.0829) and
	(165.8091,49.3886) .. (157.0000,36.8081) .. controls (156.9136,36.6837) and
	(156.8368,36.5567) .. (156.7500,36.4331) .. controls (156.0718,35.4748) and
	(155.3864,34.5612) .. (154.6875,33.6519) .. controls (153.9623,32.7083) and
	(153.2426,31.7917) .. (152.5000,30.9019) .. controls (152.4390,30.8288) and
	(152.3736,30.7558) .. (152.3125,30.6831) .. controls (152.2599,30.6202) and
	(152.2089,30.5582) .. (152.1562,30.4956) .. controls (151.3928,29.5920) and
	(150.6196,28.7165) .. (149.8438,27.8706) .. controls (149.8183,27.8428) and
	(149.8067,27.8046) .. (149.7812,27.7769) .. controls (149.2845,27.2367) and
	(148.7500,26.7618) .. (148.2500,26.2456) .. controls (147.9417,25.9271) and
	(147.6539,25.5870) .. (147.3438,25.2769) -- (147.3438,25.2769) -- cycle;
	%blue-top
	\shade[left color=cbccadf,right color=cedf7f7]
	(86.7188,0.0269) .. controls (65.0396,0.0269) and
	(46.7961,7.8182) .. (33.5000,17.9644) .. controls (32.5547,18.6857) and
	(31.6449,19.4086) .. (30.7500,20.1519) .. controls (30.6537,20.2318) and
	(30.5644,20.3217) .. (30.4688,20.4019) .. controls (30.1195,20.6958) and
	(29.7782,20.9808) .. (29.4375,21.2769) .. controls (28.8610,21.7760) and
	(28.2725,22.2703) .. (27.7188,22.7769) .. controls (27.1473,23.3006) and
	(26.6385,23.8432) .. (26.0938,24.3706) .. controls (40.1613,12.0068) and
	(65.4731,24.3402) .. (86.4062,39.6519) .. controls (107.2671,24.3635) and
	(132.3808,12.1038) .. (146.3750,24.4019) .. controls (145.0285,23.0977) and
	(143.6332,21.7702) .. (142.1250,20.4956) .. controls (141.1927,19.7077) and
	(140.2425,18.9521) .. (139.2500,18.1831) .. controls (139.1461,18.1026) and
	(139.0421,18.0134) .. (138.9375,17.9331) .. controls (138.0439,17.2513) and
	(137.1252,16.5607) .. (136.1875,15.9019) .. controls (136.0549,15.8080) and
	(135.9148,15.7140) .. (135.7812,15.6206) .. controls (135.5784,15.4798) and
	(135.3611,15.3540) .. (135.1562,15.2144) .. controls (134.2700,14.6103) and
	(133.3921,14.0136) .. (132.4688,13.4331) .. controls (121.1009,6.2864) and
	(106.9492,1.0175) .. (90.6875,0.1206) .. controls (90.1934,0.0934) and
	(89.6855,0.0772) .. (89.1875,0.0581) .. controls (89.1146,0.0560) and
	(89.0417,0.0600) .. (88.9688,0.0581) .. controls (88.2180,0.0383) and
	(87.4787,0.0269) .. (86.7188,0.0269) -- (86.7188,0.0269) -- cycle;
	%yellow-top
	\shade[top color=cffd5ba,bottom color=cfff9da]
	(148.2500,26.2456) .. controls (160.5323,40.2559) and
	(148.3422,65.4886) .. (133.0938,86.4019) .. controls (148.3381,107.3217) and
	(160.5533,132.5271) .. (148.2500,146.5269) .. controls (149.5544,145.1799) and
	(150.8502,143.7855) .. (152.1250,142.2769) .. controls (152.9130,141.3443) and
	(153.6996,140.3946) .. (154.4688,139.4019) .. controls (154.8867,138.8624) and
	(155.3077,138.3025) .. (155.7188,137.7456) .. controls (156.1536,137.1519) and
	(156.5752,136.5451) .. (157.0000,135.9331) .. controls (157.7502,134.8523) and
	(158.5038,133.7577) .. (159.2188,132.6206) .. controls (166.6518,120.7981) and
	(172.0254,105.9774) .. (172.5938,88.9019) .. controls (172.6099,88.2234) and
	(172.6250,87.5566) .. (172.6250,86.8706) .. controls (172.6250,66.8606) and
	(166.0051,49.7772) .. (157.0000,36.8081) .. controls (156.9135,36.6837) and
	(156.8367,36.5567) .. (156.7500,36.4331) .. controls (156.0717,35.4748) and
	(155.3863,34.5612) .. (154.6875,33.6519) .. controls (153.9657,32.7062) and
	(153.2436,31.7972) .. (152.5000,30.9019) .. controls (152.4390,30.8288) and
	(152.3735,30.7558) .. (152.3125,30.6831) .. controls (152.2599,30.6202) and
	(152.2089,30.5582) .. (152.1562,30.4956) .. controls (151.3927,29.5920) and
	(150.6195,28.7165) .. (149.8437,27.8706) .. controls (149.8182,27.8428) and
	(149.8067,27.8046) .. (149.7812,27.7769) .. controls (149.2845,27.2367) and
	(148.7499,26.7618) .. (148.2500,26.2456) -- (148.2500,26.2456) -- cycle;
\end{tikzpicture}
}}

\definecolor{igaliaColor}{rgb}{0.423529,0.560784,0.761718}


%% Float-controlling commands
%
%\renewcommand\topfraction{0.9}
%\renewcommand\bottomfraction{0.8}
%\renewcommand\dbltopfraction{0.9}
%\renewcommand\textfraction{0.07}
%\renewcommand\floatpagefraction{0.7}
%\renewcommand\dblfloatpagefraction{0.7}
%\setcounter{topnumber}{2}
%\setcounter{bottomnumber}{2}
%\setcounter{totalnumber}{4}
%\setcounter{dbltopnumber}{2}


\if@twoside
  \def\ps@headings{
      \let\@oddfoot\@empty\let\@evenfoot\@empty
      \def\@evenhead{\thepage\hfil\slshape\leftmark}
      \def\@oddhead{\textcolor{igaliaColor}{{\slshape\rightmark}\hfil\thepage}}
      \let\@mkboth\markboth
    \def\chaptermark##1{
      \markboth {\MakeUppercase{
        \ifnum \c@secnumdepth >\m@ne
            \@chapapp\ \thechapter. \
        \fi
        ##1}}{}}
    \def\sectionmark##1{
      \markright {\MakeUppercase{
        \ifnum \c@secnumdepth >\z@
          \thesection. \
        \fi
        ##1}}}}
\else
  \def\ps@headings{
%    \let\@oddfoot\@empty

    \def\@oddhead{\textcolor{igaliaColor}{{\slshape\rightmark\hfil\@author}}}
   \def\@oddfoot{\textcolor{igaliaColor}{\expandafter\@title\hfil\thepage}}

    \let\@mkboth\markboth
    \def\chaptermark##1{
      \markright {\MakeUppercase{
        \ifnum \c@secnumdepth >\m@ne
            \@chapapp\ \thechapter. \
        \fi
        ##1}}}}
\fi

\def\ps@myheadings{
    \let\@oddfoot\@empty\let\@evenfoot\@empty
    \def\@evenhead{\textcolor{igaliaColor}{\thepage\hfil\slshape\leftmark}}
    \def\@oddhead{\textcolor{igaliaColor}{{\slshape\rightmark}\hfil\thepage}}
    \let\@mkboth\@gobbletwo
    \let\chaptermark\@gobble
    \let\sectionmark\@gobble
    }

\renewcommand{\maketitle}{%
	%\hypersetup{pdftitle={\@title},pdfauthor={\@author}}
	\begin{titlepage}
	  \let\footnotesize\small
	\let\footnoterule\relax
	  \let \footnote \thanks
	\vspace*{-2cm}
	  \begin{flushright}
	\usebox{\igaliaMiniLogo}
	  \end{flushright}
	\vspace*{2cm}
	  \null\vfil
	\vskip 60\p@
	  \begin{center}
	{\sf\bfseries\Huge\@title\par}
      \vskip 6cm
	\end{center}\par
	\AddToShipoutPicture*{
	\put(-220,-220){\usebox\igaliaShipoutLogo}
	}
	\begin{flushright}
	\begin{tabular}[t]{l}
	\vspace*{1cm}{\huge \@customer} \\
        \vspace*{0.5cm}{\LARGE \@reference} \\
	{\LARGE \@city\xspace \@date} \\
      \end{tabular}
	\end{flushright}
	\@thanks
	\vfil\null
  \end{titlepage}
}%
\def\@maketitle{
  \newpage
  \null
  \vskip 2em
  \vspace*{-2cm}
  \begin{flushright}
      \usebox{\igaliaMiniLogo}
  \end{flushright}
  \vspace*{2cm}
  \begin{center}
  \let \footnote \thanks
    {\Huge \@title \par}
      \vskip 6cm
  \end{center}\par
  \begin{flushright}
        \begin{tabular}[t]{l}
           {\huge \@customer} \par \\
           {\LARGE \@reference}\\
           {\LARGE \@city,\xspace \@date}\\
        \end{tabular}
  \end{flushright}
  \par
  \vskip 1.5em}

% Set styles for headings, etc
\setkomafont{pagehead}{\sffamily\color{igaliaColor}}
\setkomafont{pagenumber}{\sffamily\bfseries\color{igaliaColor}}

% Tweak sectioning styles
\addtokomafont{chapter}{\textcolor{igaliaColor}}
\addtokomafont{paragraph}{\textcolor{igaliaColor}}
\addtokomafont{subsection}{\textcolor{igaliaColor}}
\addtokomafont{subparagraph}{\normalfont\rmfamily\bfseries\textcolor{igaliaColor}}

\renewcommand{\@@makeschapterhead}[1]{
	\chapterheadstartvskip%
	{\normalfont\sectfont\nobreak\size@chapter{}%
	\setlength{\parindent}{\z@}\setlength{\parfillskip}{\fill}%
	\raggedsection \interlinepenalty \@M \size@chapter{#1}\par
	\vspace{0.5em}\color{igaliaColor}{\hrule height 2pt}\vspace{2em}}
	\nobreak\chapterheadendvskip%
}

\renewcommand{\@@makechapterhead}[1]{
	\chapterheadstartvskip%
	{\setlength{\parindent}{\z@}\setlength{\parfillskip}{\fill}%
	\normalfont\sectfont\nobreak\size@chapter{}%
	\if@chapterprefix
	\let\@tempa\raggedsection
	\else
	\let\@tempa\@hangfrom
	\fi
	\@tempa{\ifnum \c@secnumdepth >\m@ne%
	\if@mainmatter
	\if@chapterprefix
	\expandafter\size@chapterprefix
	\else
	\expandafter\size@chapter
	\fi
	{\chapterformat}%
	\if@chapterprefix
	\size@chapterprefix{}\endgraf\nobreak\vskip.5\baselineskip
	\fi
	\fi
	\fi
	}%
	{\raggedsection \interlinepenalty \@M \size@chapter{#1}\par
	\vspace{0.5em}\color{igaliaColor}{\hrule height 2pt}}\vspace{2em}}
	\nobreak\chapterheadendvskip
}

\parskip=6pt
\parindent=0pt

\renewcommand{\textfraction}{0.05}
\renewcommand{\topfraction}{0.95}
\renewcommand{\bottomfraction}{0.95}
\renewcommand{\floatpagefraction}{0.35}
\setcounter{totalnumber}{5}

\endinput
