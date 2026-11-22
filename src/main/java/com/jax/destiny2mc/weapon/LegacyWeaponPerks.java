package com.jax.destiny2mc.weapon;

import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.gunsmith.GunsmithRecipes;
import com.jax.destiny2mc.item.DestinyGunItem;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class LegacyWeaponPerks {
   public static final int[] POOLS;
   public static boolean jsonLoaded = false;

   static {
      int[] var0 = POOLS = new int[832];
      var0[0] = 104;
      var0[1] = 101;
      var0[2] = 103;
      var0[3] = 106;
      var0[4] = 203;
      var0[5] = 202;
      var0[6] = 205;
      var0[7] = 206;
      var0[8] = 303;
      var0[9] = 302;
      var0[10] = 305;
      var0[11] = 301;
      var0[12] = 402;
      var0[13] = 404;
      var0[14] = 401;
      var0[15] = 406;
      var0[16] = 101;
      var0[17] = 103;
      var0[18] = 106;
      var0[19] = 104;
      var0[20] = 202;
      var0[21] = 205;
      var0[22] = 206;
      var0[23] = 203;
      var0[24] = 302;
      var0[25] = 305;
      var0[26] = 301;
      var0[27] = 303;
      var0[28] = 404;
      var0[29] = 401;
      var0[30] = 406;
      var0[31] = 402;
      var0[32] = 103;
      var0[33] = 106;
      var0[34] = 104;
      var0[35] = 101;
      var0[36] = 205;
      var0[37] = 206;
      var0[38] = 203;
      var0[39] = 202;
      var0[40] = 305;
      var0[41] = 301;
      var0[42] = 303;
      var0[43] = 302;
      var0[44] = 401;
      var0[45] = 406;
      var0[46] = 402;
      var0[47] = 404;
      var0[48] = 106;
      var0[49] = 104;
      var0[50] = 101;
      var0[51] = 103;
      var0[52] = 206;
      var0[53] = 203;
      var0[54] = 202;
      var0[55] = 205;
      var0[56] = 301;
      var0[57] = 303;
      var0[58] = 302;
      var0[59] = 305;
      var0[60] = 406;
      var0[61] = 402;
      var0[62] = 404;
      var0[63] = 401;
      var0[64] = 104;
      var0[65] = 101;
      var0[66] = 103;
      var0[67] = 106;
      var0[68] = 203;
      var0[69] = 202;
      var0[70] = 205;
      var0[71] = 206;
      var0[72] = 303;
      var0[73] = 302;
      var0[74] = 305;
      var0[75] = 301;
      var0[76] = 402;
      var0[77] = 404;
      var0[78] = 401;
      var0[79] = 406;
      var0[80] = 106;
      var0[81] = 103;
      var0[82] = 105;
      var0[83] = 101;
      var0[84] = 204;
      var0[85] = 205;
      var0[86] = 206;
      var0[87] = 201;
      var0[88] = 302;
      var0[89] = 306;
      var0[90] = 304;
      var0[91] = 301;
      var0[92] = 405;
      var0[93] = 402;
      var0[94] = 406;
      var0[95] = 401;
      var0[96] = 103;
      var0[97] = 105;
      var0[98] = 101;
      var0[99] = 106;
      var0[100] = 205;
      var0[101] = 206;
      var0[102] = 201;
      var0[103] = 204;
      var0[104] = 306;
      var0[105] = 304;
      var0[106] = 301;
      var0[107] = 302;
      var0[108] = 402;
      var0[109] = 406;
      var0[110] = 401;
      var0[111] = 405;
      var0[112] = 105;
      var0[113] = 101;
      var0[114] = 106;
      var0[115] = 103;
      var0[116] = 206;
      var0[117] = 201;
      var0[118] = 204;
      var0[119] = 205;
      var0[120] = 304;
      var0[121] = 301;
      var0[122] = 302;
      var0[123] = 306;
      var0[124] = 406;
      var0[125] = 401;
      var0[126] = 405;
      var0[127] = 402;
      var0[128] = 101;
      var0[129] = 106;
      var0[130] = 103;
      var0[131] = 105;
      var0[132] = 201;
      var0[133] = 204;
      var0[134] = 205;
      var0[135] = 206;
      var0[136] = 301;
      var0[137] = 302;
      var0[138] = 306;
      var0[139] = 304;
      var0[140] = 401;
      var0[141] = 405;
      var0[142] = 402;
      var0[143] = 406;
      var0[144] = 106;
      var0[145] = 103;
      var0[146] = 105;
      var0[147] = 101;
      var0[148] = 204;
      var0[149] = 205;
      var0[150] = 206;
      var0[151] = 201;
      var0[152] = 302;
      var0[153] = 306;
      var0[154] = 304;
      var0[155] = 301;
      var0[156] = 405;
      var0[157] = 402;
      var0[158] = 406;
      var0[159] = 401;
      var0[160] = 101;
      var0[161] = 102;
      var0[162] = 104;
      var0[163] = 105;
      var0[164] = 201;
      var0[165] = 204;
      var0[166] = 202;
      var0[167] = 203;
      var0[168] = 305;
      var0[169] = 306;
      var0[170] = 304;
      var0[171] = 302;
      var0[172] = 405;
      var0[173] = 401;
      var0[174] = 403;
      var0[175] = 404;
      var0[176] = 102;
      var0[177] = 104;
      var0[178] = 105;
      var0[179] = 101;
      var0[180] = 204;
      var0[181] = 202;
      var0[182] = 203;
      var0[183] = 201;
      var0[184] = 306;
      var0[185] = 304;
      var0[186] = 302;
      var0[187] = 305;
      var0[188] = 401;
      var0[189] = 403;
      var0[190] = 404;
      var0[191] = 405;
      var0[192] = 104;
      var0[193] = 105;
      var0[194] = 101;
      var0[195] = 102;
      var0[196] = 202;
      var0[197] = 203;
      var0[198] = 201;
      var0[199] = 204;
      var0[200] = 304;
      var0[201] = 302;
      var0[202] = 305;
      var0[203] = 306;
      var0[204] = 403;
      var0[205] = 404;
      var0[206] = 405;
      var0[207] = 401;
      var0[208] = 105;
      var0[209] = 101;
      var0[210] = 102;
      var0[211] = 104;
      var0[212] = 203;
      var0[213] = 201;
      var0[214] = 204;
      var0[215] = 202;
      var0[216] = 302;
      var0[217] = 305;
      var0[218] = 306;
      var0[219] = 304;
      var0[220] = 404;
      var0[221] = 405;
      var0[222] = 401;
      var0[223] = 403;
      var0[224] = 101;
      var0[225] = 102;
      var0[226] = 104;
      var0[227] = 105;
      var0[228] = 201;
      var0[229] = 204;
      var0[230] = 202;
      var0[231] = 203;
      var0[232] = 305;
      var0[233] = 306;
      var0[234] = 304;
      var0[235] = 302;
      var0[236] = 405;
      var0[237] = 401;
      var0[238] = 403;
      var0[239] = 404;
      var0[240] = 102;
      var0[241] = 105;
      var0[242] = 106;
      var0[243] = 103;
      var0[244] = 204;
      var0[245] = 201;
      var0[246] = 203;
      var0[247] = 202;
      var0[248] = 305;
      var0[249] = 303;
      var0[250] = 306;
      var0[251] = 301;
      var0[252] = 406;
      var0[253] = 403;
      var0[254] = 402;
      var0[255] = 401;
      var0[256] = 105;
      var0[257] = 106;
      var0[258] = 103;
      var0[259] = 102;
      var0[260] = 201;
      var0[261] = 203;
      var0[262] = 202;
      var0[263] = 204;
      var0[264] = 303;
      var0[265] = 306;
      var0[266] = 301;
      var0[267] = 305;
      var0[268] = 403;
      var0[269] = 402;
      var0[270] = 401;
      var0[271] = 406;
      var0[272] = 106;
      var0[273] = 103;
      var0[274] = 102;
      var0[275] = 105;
      var0[276] = 203;
      var0[277] = 202;
      var0[278] = 204;
      var0[279] = 201;
      var0[280] = 306;
      var0[281] = 301;
      var0[282] = 305;
      var0[283] = 303;
      var0[284] = 402;
      var0[285] = 401;
      var0[286] = 406;
      var0[287] = 403;
      var0[288] = 103;
      var0[289] = 102;
      var0[290] = 105;
      var0[291] = 106;
      var0[292] = 202;
      var0[293] = 204;
      var0[294] = 201;
      var0[295] = 203;
      var0[296] = 301;
      var0[297] = 305;
      var0[298] = 303;
      var0[299] = 306;
      var0[300] = 401;
      var0[301] = 406;
      var0[302] = 403;
      var0[303] = 402;
      var0[304] = 102;
      var0[305] = 105;
      var0[306] = 106;
      var0[307] = 103;
      var0[308] = 204;
      var0[309] = 201;
      var0[310] = 203;
      var0[311] = 202;
      var0[312] = 305;
      var0[313] = 303;
      var0[314] = 306;
      var0[315] = 301;
      var0[316] = 406;
      var0[317] = 403;
      var0[318] = 402;
      var0[319] = 401;
      var0[320] = 104;
      var0[321] = 101;
      var0[322] = 103;
      var0[323] = 106;
      var0[324] = 203;
      var0[325] = 202;
      var0[326] = 205;
      var0[327] = 206;
      var0[328] = 303;
      var0[329] = 302;
      var0[330] = 305;
      var0[331] = 301;
      var0[332] = 402;
      var0[333] = 404;
      var0[334] = 401;
      var0[335] = 406;
      var0[336] = 106;
      var0[337] = 103;
      var0[338] = 105;
      var0[339] = 101;
      var0[340] = 204;
      var0[341] = 205;
      var0[342] = 206;
      var0[343] = 201;
      var0[344] = 302;
      var0[345] = 306;
      var0[346] = 304;
      var0[347] = 301;
      var0[348] = 405;
      var0[349] = 402;
      var0[350] = 406;
      var0[351] = 401;
      var0[352] = 101;
      var0[353] = 102;
      var0[354] = 104;
      var0[355] = 105;
      var0[356] = 201;
      var0[357] = 204;
      var0[358] = 202;
      var0[359] = 203;
      var0[360] = 305;
      var0[361] = 306;
      var0[362] = 304;
      var0[363] = 302;
      var0[364] = 405;
      var0[365] = 401;
      var0[366] = 403;
      var0[367] = 404;
      var0[368] = 102;
      var0[369] = 105;
      var0[370] = 106;
      var0[371] = 103;
      var0[372] = 204;
      var0[373] = 201;
      var0[374] = 203;
      var0[375] = 202;
      var0[376] = 305;
      var0[377] = 303;
      var0[378] = 306;
      var0[379] = 301;
      var0[380] = 406;
      var0[381] = 403;
      var0[382] = 402;
      var0[383] = 401;
      var0[384] = 104;
      var0[385] = 101;
      var0[386] = 103;
      var0[387] = 106;
      var0[388] = 203;
      var0[389] = 202;
      var0[390] = 205;
      var0[391] = 206;
      var0[392] = 303;
      var0[393] = 302;
      var0[394] = 305;
      var0[395] = 301;
      var0[396] = 402;
      var0[397] = 404;
      var0[398] = 401;
      var0[399] = 406;
      var0[400] = 106;
      var0[401] = 103;
      var0[402] = 105;
      var0[403] = 101;
      var0[404] = 204;
      var0[405] = 205;
      var0[406] = 206;
      var0[407] = 201;
      var0[408] = 302;
      var0[409] = 306;
      var0[410] = 304;
      var0[411] = 301;
      var0[412] = 405;
      var0[413] = 402;
      var0[414] = 406;
      var0[415] = 401;
      var0[416] = 101;
      var0[417] = 102;
      var0[418] = 104;
      var0[419] = 105;
      var0[420] = 201;
      var0[421] = 204;
      var0[422] = 202;
      var0[423] = 203;
      var0[424] = 305;
      var0[425] = 306;
      var0[426] = 304;
      var0[427] = 302;
      var0[428] = 405;
      var0[429] = 401;
      var0[430] = 403;
      var0[431] = 404;
      var0[432] = 102;
      var0[433] = 105;
      var0[434] = 106;
      var0[435] = 103;
      var0[436] = 204;
      var0[437] = 201;
      var0[438] = 203;
      var0[439] = 202;
      var0[440] = 305;
      var0[441] = 303;
      var0[442] = 306;
      var0[443] = 301;
      var0[444] = 406;
      var0[445] = 403;
      var0[446] = 402;
      var0[447] = 401;
      var0[448] = 101;
      var0[449] = 103;
      var0[450] = 102;
      var0[451] = 104;
      var0[452] = 202;
      var0[453] = 206;
      var0[454] = 203;
      var0[455] = 201;
      var0[456] = 301;
      var0[457] = 303;
      var0[458] = 302;
      var0[459] = 306;
      var0[460] = 406;
      var0[461] = 402;
      var0[462] = 404;
      var0[463] = 405;
      var0[464] = 101;
      var0[465] = 102;
      var0[466] = 104;
      var0[467] = 105;
      var0[468] = 206;
      var0[469] = 203;
      var0[470] = 201;
      var0[471] = 202;
      var0[472] = 303;
      var0[473] = 301;
      var0[474] = 305;
      var0[475] = 302;
      var0[476] = 406;
      var0[477] = 402;
      var0[478] = 401;
      var0[479] = 404;
      var0[480] = 101;
      var0[481] = 103;
      var0[482] = 102;
      var0[483] = 105;
      var0[484] = 202;
      var0[485] = 201;
      var0[486] = 205;
      var0[487] = 203;
      var0[488] = 302;
      var0[489] = 305;
      var0[490] = 304;
      var0[491] = 301;
      var0[492] = 405;
      var0[493] = 403;
      var0[494] = 404;
      var0[495] = 406;
      var0[496] = 101;
      var0[497] = 104;
      var0[498] = 103;
      var0[499] = 102;
      var0[500] = 206;
      var0[501] = 202;
      var0[502] = 203;
      var0[503] = 201;
      var0[504] = 302;
      var0[505] = 303;
      var0[506] = 301;
      var0[507] = 306;
      var0[508] = 406;
      var0[509] = 401;
      var0[510] = 404;
      var0[511] = 402;
      var0[512] = 102;
      var0[513] = 103;
      var0[514] = 104;
      var0[515] = 101;
      var0[516] = 201;
      var0[517] = 202;
      var0[518] = 206;
      var0[519] = 203;
      var0[520] = 301;
      var0[521] = 303;
      var0[522] = 306;
      var0[523] = 302;
      var0[524] = 406;
      var0[525] = 401;
      var0[526] = 402;
      var0[527] = 405;
      var0[528] = 105;
      var0[529] = 106;
      var0[530] = 102;
      var0[531] = 103;
      var0[532] = 201;
      var0[533] = 204;
      var0[534] = 203;
      var0[535] = 202;
      var0[536] = 303;
      var0[537] = 306;
      var0[538] = 301;
      var0[539] = 302;
      var0[540] = 402;
      var0[541] = 403;
      var0[542] = 406;
      var0[543] = 404;
      var0[544] = 101;
      var0[545] = 104;
      var0[546] = 102;
      var0[547] = 105;
      var0[548] = 204;
      var0[549] = 203;
      var0[550] = 205;
      var0[551] = 206;
      var0[552] = 305;
      var0[553] = 303;
      var0[554] = 304;
      var0[555] = 302;
      var0[556] = 406;
      var0[557] = 403;
      var0[558] = 402;
      var0[559] = 404;
      var0[560] = 101;
      var0[561] = 104;
      var0[562] = 102;
      var0[563] = 103;
      var0[564] = 206;
      var0[565] = 202;
      var0[566] = 201;
      var0[567] = 205;
      var0[568] = 302;
      var0[569] = 301;
      var0[570] = 303;
      var0[571] = 306;
      var0[572] = 402;
      var0[573] = 406;
      var0[574] = 404;
      var0[575] = 401;
      var0[576] = 102;
      var0[577] = 103;
      var0[578] = 104;
      var0[579] = 105;
      var0[580] = 201;
      var0[581] = 202;
      var0[582] = 206;
      var0[583] = 203;
      var0[584] = 301;
      var0[585] = 305;
      var0[586] = 306;
      var0[587] = 302;
      var0[588] = 405;
      var0[589] = 406;
      var0[590] = 401;
      var0[591] = 404;
      var0[592] = 101;
      var0[593] = 102;
      var0[594] = 104;
      var0[595] = 105;
      var0[596] = 205;
      var0[597] = 202;
      var0[598] = 206;
      var0[599] = 201;
      var0[600] = 305;
      var0[601] = 302;
      var0[602] = 304;
      var0[603] = 301;
      var0[604] = 406;
      var0[605] = 404;
      var0[606] = 403;
      var0[607] = 402;
      var0[608] = 103;
      var0[609] = 101;
      var0[610] = 102;
      var0[611] = 105;
      var0[612] = 203;
      var0[613] = 202;
      var0[614] = 204;
      var0[615] = 205;
      var0[616] = 305;
      var0[617] = 304;
      var0[618] = 302;
      var0[619] = 306;
      var0[620] = 403;
      var0[621] = 404;
      var0[622] = 405;
      var0[623] = 401;
      var0[624] = 106;
      var0[625] = 105;
      var0[626] = 102;
      var0[627] = 104;
      var0[628] = 201;
      var0[629] = 203;
      var0[630] = 204;
      var0[631] = 202;
      var0[632] = 306;
      var0[633] = 303;
      var0[634] = 301;
      var0[635] = 305;
      var0[636] = 401;
      var0[637] = 403;
      var0[638] = 406;
      var0[639] = 402;
      var0[640] = 106;
      var0[641] = 105;
      var0[642] = 102;
      var0[643] = 104;
      var0[644] = 201;
      var0[645] = 203;
      var0[646] = 204;
      var0[647] = 202;
      var0[648] = 306;
      var0[649] = 303;
      var0[650] = 301;
      var0[651] = 305;
      var0[652] = 401;
      var0[653] = 403;
      var0[654] = 406;
      var0[655] = 402;
      var0[656] = 106;
      var0[657] = 105;
      var0[658] = 102;
      var0[659] = 104;
      var0[660] = 201;
      var0[661] = 203;
      var0[662] = 204;
      var0[663] = 202;
      var0[664] = 306;
      var0[665] = 303;
      var0[666] = 301;
      var0[667] = 305;
      var0[668] = 401;
      var0[669] = 403;
      var0[670] = 406;
      var0[671] = 402;
      var0[672] = 106;
      var0[673] = 105;
      var0[674] = 102;
      var0[675] = 104;
      var0[676] = 201;
      var0[677] = 203;
      var0[678] = 204;
      var0[679] = 202;
      var0[680] = 306;
      var0[681] = 303;
      var0[682] = 301;
      var0[683] = 305;
      var0[684] = 401;
      var0[685] = 403;
      var0[686] = 406;
      var0[687] = 402;
      var0[688] = 106;
      var0[689] = 105;
      var0[690] = 102;
      var0[691] = 104;
      var0[692] = 201;
      var0[693] = 203;
      var0[694] = 204;
      var0[695] = 202;
      var0[696] = 306;
      var0[697] = 303;
      var0[698] = 301;
      var0[699] = 305;
      var0[700] = 401;
      var0[701] = 403;
      var0[702] = 406;
      var0[703] = 402;
      var0[704] = 103;
      var0[705] = 101;
      var0[706] = 102;
      var0[707] = 105;
      var0[708] = 203;
      var0[709] = 202;
      var0[710] = 204;
      var0[711] = 205;
      var0[712] = 305;
      var0[713] = 304;
      var0[714] = 302;
      var0[715] = 306;
      var0[716] = 403;
      var0[717] = 404;
      var0[718] = 405;
      var0[719] = 401;
      var0[720] = 103;
      var0[721] = 101;
      var0[722] = 102;
      var0[723] = 105;
      var0[724] = 203;
      var0[725] = 202;
      var0[726] = 204;
      var0[727] = 205;
      var0[728] = 305;
      var0[729] = 304;
      var0[730] = 302;
      var0[731] = 306;
      var0[732] = 403;
      var0[733] = 404;
      var0[734] = 405;
      var0[735] = 401;
      var0[736] = 101;
      var0[737] = 103;
      var0[738] = 106;
      var0[739] = 104;
      var0[740] = 202;
      var0[741] = 205;
      var0[742] = 206;
      var0[743] = 203;
      var0[744] = 302;
      var0[745] = 305;
      var0[746] = 301;
      var0[747] = 303;
      var0[748] = 404;
      var0[749] = 401;
      var0[750] = 406;
      var0[751] = 402;
      var0[752] = 101;
      var0[753] = 103;
      var0[754] = 106;
      var0[755] = 104;
      var0[756] = 202;
      var0[757] = 205;
      var0[758] = 206;
      var0[759] = 203;
      var0[760] = 302;
      var0[761] = 305;
      var0[762] = 301;
      var0[763] = 303;
      var0[764] = 404;
      var0[765] = 401;
      var0[766] = 406;
      var0[767] = 402;
      var0[768] = 106;
      var0[769] = 105;
      var0[770] = 102;
      var0[771] = 104;
      var0[772] = 201;
      var0[773] = 203;
      var0[774] = 204;
      var0[775] = 202;
      var0[776] = 306;
      var0[777] = 303;
      var0[778] = 301;
      var0[779] = 305;
      var0[780] = 401;
      var0[781] = 403;
      var0[782] = 406;
      var0[783] = 402;
      var0[784] = 106;
      var0[785] = 105;
      var0[786] = 102;
      var0[787] = 104;
      var0[788] = 201;
      var0[789] = 203;
      var0[790] = 204;
      var0[791] = 202;
      var0[792] = 306;
      var0[793] = 303;
      var0[794] = 301;
      var0[795] = 305;
      var0[796] = 401;
      var0[797] = 403;
      var0[798] = 406;
      var0[799] = 402;
      var0[800] = 106;
      var0[801] = 105;
      var0[802] = 102;
      var0[803] = 104;
      var0[804] = 201;
      var0[805] = 203;
      var0[806] = 204;
      var0[807] = 202;
      var0[808] = 306;
      var0[809] = 303;
      var0[810] = 301;
      var0[811] = 305;
      var0[812] = 401;
      var0[813] = 403;
      var0[814] = 406;
      var0[815] = 402;
      var0[816] = 101;
      var0[817] = 102;
      var0[818] = 104;
      var0[819] = 105;
      var0[820] = 201;
      var0[821] = 204;
      var0[822] = 202;
      var0[823] = 203;
      var0[824] = 305;
      var0[825] = 306;
      var0[826] = 304;
      var0[827] = 302;
      var0[828] = 405;
      var0[829] = 401;
      var0[830] = 403;
      var0[831] = 404;
      ensureJsonLoaded();
   }

   public static void ensureJsonLoaded() {
      if (!jsonLoaded) {
         jsonLoaded = true;
         // This inherited table is packaged with the mod. Explicitly close its stream.
         try (InputStream stream = LegacyWeaponPerks.class.getResourceAsStream("/assets/destiny2mc/perks/perk_pools.json")) {
            if (stream != null) parseJsonOverrides(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
         } catch (java.io.IOException failure) {
            throw new java.io.UncheckedIOException("Unable to read bundled weapon perk pools", failure);
         }
      }
   }

   private static void parseJsonOverrides(String var0) {
      extract16Ints(var0, "\"khvostov_7g_0x\"", 0);
      extract16Ints(var0, "\"ammit_ar2\"", 16);
      extract16Ints(var0, "\"sweet_business\"", 32);
      extract16Ints(var0, "\"monte_carlo\"", 48);
      extract16Ints(var0, "\"cerberus_plus_1\"", 64);
      extract16Ints(var0, "\"ace_of_spades\"", 80);
      extract16Ints(var0, "\"sunshot\"", 96);
      extract16Ints(var0, "\"thorn\"", 112);
      extract16Ints(var0, "\"crimson\"", 128);
      extract16Ints(var0, "\"the_last_word\"", 144);
      extract16Ints(var0, "\"izanagis_burden\"", 160);
      extract16Ints(var0, "\"cloudstrike\"", 176);
      extract16Ints(var0, "\"whisper_of_the_worm\"", 192);
      extract16Ints(var0, "\"darci\"", 208);
      extract16Ints(var0, "\"ice_breaker\"", 224);
      extract16Ints(var0, "\"the_chaperone\"", 240);
      extract16Ints(var0, "\"ikelos_sg_v103\"", 256);
      extract16Ints(var0, "\"conditional_finality\"", 272);
      extract16Ints(var0, "\"tractor_cannon\"", 288);
      extract16Ints(var0, "\"lord_of_wolves\"", 304);
      extract16Ints(var0, "\"blood_feud\"", 320);
      extract16Ints(var0, "\"brigands_law\"", 336);
      extract16Ints(var0, "\"tarnished_mettle\"", 352);
      extract16Ints(var0, "\"no_reprieve\"", 368);
      extract16Ints(var0, "\"wrongful_ingress\"", 384);
      extract16Ints(var0, "\"parole\"", 400);
      extract16Ints(var0, "\"total_lockdown\"", 416);
      extract16Ints(var0, "\"eternal_retribution\"", 432);
      extract16Ints(var0, "\"necrochasm\"", 448);
      extract16Ints(var0, "\"abyss_defiant\"", 464);
      extract16Ints(var0, "\"fang_of_ir_yut\"", 480);
      extract16Ints(var0, "\"oversoul_edict\"", 496);
      extract16Ints(var0, "\"word_of_crota\"", 512);
      extract16Ints(var0, "\"swordbreaker\"", 528);
      extract16Ints(var0, "\"song_of_ir_yut\"", 544);
      extract16Ints(var0, "\"vex_mythoclast\"", 560);
      extract16Ints(var0, "\"fatebringer\"", 576);
      extract16Ints(var0, "\"vision_of_confluence\"", 592);
      extract16Ints(var0, "\"praedyths_revenge\"", 608);
      extract16Ints(var0, "\"found_verdict\"", 624);
      extract16Ints(var0, "\"telesto\"", 640);
      extract16Ints(var0, "\"jotunn\"", 656);
      extract16Ints(var0, "\"merciless\"", 672);
      extract16Ints(var0, "\"riptide\"", 688);
      extract16Ints(var0, "\"sleeper_simulant\"", 704);
      extract16Ints(var0, "\"taipan_4fr\"", 720);
      extract16Ints(var0, "\"coldheart\"", 736);
      extract16Ints(var0, "\"prometheus_lens\"", 752);
      extract16Ints(var0, "\"gjallarhorn\"", 768);
      extract16Ints(var0, "\"two_tailed_fox\"", 784);
      extract16Ints(var0, "\"witherhoard\"", 800);
      extract16Ints(var0, "\"le_monarque\"", 816);
   }

   private static void extract16Ints(String var0, String var1, int var2) {
      int var3 = var0.indexOf(var1);
      if (var3 >= 0) {
         int var4 = var0.length();
         int var5 = var3 + var1.length();
         int var6 = 0;

         for (int var7 = 0; var5 < var4 && var6 < 16; var5++) {
            char var8 = var0.charAt(var5);
            if (var8 >= '0' && var8 <= '9') {
               var7 = var7 * 10 + (var8 - '0');
            } else {
               if (var7 > 100 && var7 <= 499) {
                  POOLS[var2 + var6] = var7;
                  var6++;
               }

               var7 = 0;
               if (var8 == '}' && var6 >= 16) {
                  break;
               }
            }
         }
      }
   }

   public static String getSlotKey(int var0) {
      if (var0 == 0) {
         return "d2_pk0";
      } else if (var0 == 1) {
         return "d2_pk1";
      } else {
         return var0 == 2 ? "d2_pk2" : "d2_pk3";
      }
   }

   public static String getSlotTitle(int var0) {
      if (var0 == 0) {
         return "BARREL";
      } else if (var0 == 1) {
         return "MAGAZINE";
      } else {
         return var0 == 2 ? "TRAIT 1" : "TRAIT 2";
      }
   }

   public static int getPoolOption(int var0, int var1, int var2) {
      if (var0 >= 0 && var0 < 52) {
         var1 &= 3;
         var2 &= 3;
         return POOLS[var0 * 16 + var1 * 4 + var2];
      } else {
         return 101;
      }
   }

   public static boolean isValidPerkForWeapon(int var0, int var1, int var2) {
      for (int var3 = 0; var3 < 4; var3++) {
         if (getPoolOption(var0, var1, var3) == var2) {
            return true;
         }
      }

      return false;
   }

   public static int getActivePerk(ItemStack var0, WeaponSpec var1, int var2) {
      int var3 = getPoolOption(var1.index, var2, 0);
      if (var0 != null) {
         int var4 = GunDataHelper.getInt(var0, getSlotKey(var2), var3);
         if (isValidPerkForWeapon(var1.index, var2, var4)) {
            return var4;
         }
      }

      return var3;
   }

   public static void setActivePerk(ItemStack var0, WeaponSpec var1, int var2, int var3) {
      GunDataHelper.setInt(var0, getSlotKey(var2), var3);
      int var4 = WeaponPerkRegistry.getEffectiveMagSize(var0, var1);
      int var5 = GunDataHelper.getInt(var0, "d2_mag", var1.magSize);
      if (var5 > var4) {
         GunDataHelper.setInt(var0, "d2_mag", var4);
      }
   }

   public static String getPerkName(int var0) {
      if (var0 == 101) {
         return "Hammer-Forged Rifling";
      } else if (var0 == 102) {
         return "Polygonal Rifling";
      } else if (var0 == 103) {
         return "Corkscrew Rifling";
      } else if (var0 == 104) {
         return "Arrowhead Brake";
      } else if (var0 == 105) {
         return "Full Bore";
      } else if (var0 == 106) {
         return "Smallbore";
      } else if (var0 == 201) {
         return "Accurized Rounds";
      } else if (var0 == 202) {
         return "Appended Mag";
      } else if (var0 == 203) {
         return "Tactical Mag";
      } else if (var0 == 204) {
         return "Flared Magwell";
      } else if (var0 == 205) {
         return "High-Caliber Rounds";
      } else if (var0 == 206) {
         return "Ricochet Rounds";
      } else if (var0 == 301) {
         return "Outlaw";
      } else if (var0 == 302) {
         return "Rapid Hit";
      } else if (var0 == 303) {
         return "Subsistence";
      } else if (var0 == 304) {
         return "Fourth Time's the Charm";
      } else if (var0 == 305) {
         return "Rewind Rounds";
      } else if (var0 == 306) {
         return "Opening Shot";
      } else if (var0 == 401) {
         return "Kill Clip";
      } else if (var0 == 402) {
         return "Rampage";
      } else if (var0 == 403) {
         return "Vorpal Weapon";
      } else if (var0 == 404) {
         return "Frenzy";
      } else if (var0 == 405) {
         return "Explosive Payload";
      } else if (var0 == 406) {
         return "Elemental Detonator";
      } else if (var0 == 500) {
         return "Factory finish";
      } else if (var0 == 501) {
         return "Range masterwork";
      } else if (var0 == 502) {
         return "Stability masterwork";
      } else if (var0 == 503) {
         return "Reload masterwork";
      } else if (var0 == 504) {
         return "Accuracy masterwork";
      } else if (var0 == 600) {
         return "Empty mod socket";
      } else if (var0 == 601) {
         return "Targeting adjuster";
      } else if (var0 == 602) {
         return "Backup mag";
      } else if (var0 == 603) {
         return "Counterbalance stock";
      } else {
         return var0 == 604 ? "Major spec" : "Standard Frame";
      }
   }

   public static String getPerkDesc(int var0) {
      if (var0 == 101) {
         return "+18% Range, -10% Spread";
      } else if (var0 == 102) {
         return "-22% Recoil Kick (High Stability)";
      } else if (var0 == 103) {
         return "+10% Range, -12% Recoil, -8% Reload Time";
      } else if (var0 == 104) {
         return "-45% Horizontal Drift, -12% Recoil";
      } else if (var0 == 105) {
         return "+28% Max Range, +12% Recoil Kick";
      } else if (var0 == 106) {
         return "+12% Range, -14% Recoil & Spread";
      } else if (var0 == 201) {
         return "+15% Range, -12% ADS Cone";
      } else if (var0 == 202) {
         return "+25% Magazine Capacity";
      } else if (var0 == 203) {
         return "+12% Magazine, -12% Reload Time, -8% Recoil";
      } else if (var0 == 204) {
         return "-25% Reload Time, -6% Recoil";
      } else if (var0 == 205) {
         return "+6% Impact Damage & Heavy Target Stagger";
      } else if (var0 == 206) {
         return "+8% Range, -10% Recoil & Wall-Spark Splash";
      } else if (var0 == 301) {
         return "Precision kills cut next reload time by 40%.";
      } else if (var0 == 302) {
         return "Precision hits stack x1-x5 faster reload & stability.";
      } else if (var0 == 303) {
         return "Kills partially refill the magazine from reserves.";
      } else if (var0 == 304) {
         return "4 rapid precision hits return 2 rounds to the mag.";
      } else if (var0 == 305) {
         return "Emptying the mag refills rounds based on hits landed.";
      } else if (var0 == 306) {
         return "Opening attack has +20% range, -50% spread & +10% crit.";
      } else if (var0 == 401) {
         return "Reloading after a kill grants +25% damage for 5s.";
      } else if (var0 == 402) {
         return "Kills stack Rampage x1-x3 (up to +33% damage).";
      } else if (var0 == 403) {
         return "+20% bonus damage against Bosses, Vehicles & Captains.";
      } else if (var0 == 404) {
         return "Sustained combat grants +15% damage & -25% reload time.";
      } else if (var0 == 405) {
         return "Projectiles detonate on impact for +15% blast damage.";
      } else if (var0 == 406) {
         return "Kills unleash a matching Elemental shockwave.";
      } else if (var0 == 500) {
         return "No additional modifier.";
      } else if (var0 == 501) {
         return "Effective range +10%.";
      } else if (var0 == 502) {
         return "Vertical and horizontal recoil -12%.";
      } else if (var0 == 503) {
         return "Reload time -12%.";
      } else if (var0 == 504) {
         return "Hip-fire and aimed spread -12%.";
      } else if (var0 == 600) {
         return "No additional modifier.";
      } else if (var0 == 601) {
         return "Aimed spread -15%.";
      } else if (var0 == 602) {
         return "Magazine capacity +15%, rounded up, minimum +1.";
      } else if (var0 == 603) {
         return "Horizontal recoil -20%.";
      } else {
         return var0 == 604 ? "Damage +8% against targets with at least 40 maximum health." : "Balanced factory tuning.";
      }
   }

   public static int getEffectiveMagSize(ItemStack var0, WeaponSpec var1) {
      int var2 = var1.magSize;
      int var3 = getActivePerk(var0, var1, 1);
      if (var3 == 202) {
         int var5 = var2 * 25 / 100;
         if (var5 < 1) {
            var5 = 1;
         }

         return var2 + var5;
      } else if (var3 == 203) {
         int var4 = var2 * 12 / 100;
         if (var4 < 1) {
            var4 = 1;
         }

         return var2 + var4;
      } else {
         return var2;
      }
   }

   public static float getEffectiveRange(ItemStack var0, WeaponSpec var1, long var2) {
      float var4 = var1.range;
      int var5 = getActivePerk(var0, var1, 0);
      if (var5 == 101) {
         var4 *= 1.18F;
      }

      if (var5 == 103) {
         var4 *= 1.1F;
      }

      if (var5 == 105) {
         var4 *= 1.28F;
      }

      if (var5 == 106) {
         var4 *= 1.12F;
      }

      int var6 = getActivePerk(var0, var1, 1);
      if (var6 == 201) {
         var4 *= 1.15F;
      }

      if (var6 == 206) {
         var4 *= 1.08F;
      }

      if (getActivePerk(var0, var1, 2) == 306 && var2 >= GunDataHelper.getLong(var0, "d2_os_cd")) {
         var4 *= 1.2F;
      }

      return var4;
   }

   public static float getEffectiveSpread(ItemStack var0, WeaponSpec var1, int var2, long var3) {
      float var5;
      if (var2 != 0) {
         var5 = var1.adsSpread;
      } else {
         var5 = var1.hipSpread;
      }

      int var6 = getActivePerk(var0, var1, 0);
      if (var6 == 101) {
         var5 *= 0.9F;
      }

      if (var6 == 106) {
         var5 *= 0.86F;
      }

      if (getActivePerk(var0, var1, 1) == 201 && var2 != 0) {
         var5 *= 0.88F;
      }

      if (getActivePerk(var0, var1, 2) == 306 && var3 >= GunDataHelper.getLong(var0, "d2_os_cd")) {
         var5 *= 0.5F;
      }

      return var5;
   }

   public static float getRecoilPitchMult(ItemStack var0, WeaponSpec var1) {
      float var2 = 1.0F;
      int var3 = getActivePerk(var0, var1, 0);
      if (var3 == 102) {
         var2 *= 0.78F;
      }

      if (var3 == 103) {
         var2 *= 0.88F;
      }

      if (var3 == 104) {
         var2 *= 0.88F;
      }

      if (var3 == 105) {
         var2 *= 1.12F;
      }

      if (var3 == 106) {
         var2 *= 0.86F;
      }

      int var4 = getActivePerk(var0, var1, 1);
      if (var4 == 203) {
         var2 *= 0.92F;
      }

      if (var4 == 204) {
         var2 *= 0.94F;
      }

      if (var4 == 206) {
         var2 *= 0.9F;
      }

      if (getActivePerk(var0, var1, 2) == 302) {
         int var5 = GunDataHelper.getInt(var0, "d2_rh_stk", 0);
         if (var5 > 0) {
            var2 *= 1.0F - (float)var5 * 0.04F;
         }
      }

      return var2;
   }

   public static float getRecoilYawMult(ItemStack var0, WeaponSpec var1) {
      float var2 = 1.0F;
      int var3 = getActivePerk(var0, var1, 0);
      if (var3 == 102) {
         var2 *= 0.8F;
      }

      if (var3 == 103) {
         var2 *= 0.88F;
      }

      if (var3 == 104) {
         var2 *= 0.55F;
      }

      if (var3 == 106) {
         var2 *= 0.88F;
      }

      return var2;
   }

   public static int getEffectiveReloadTicks(ItemStack var0, WeaponSpec var1, long var2) {
      float var4 = (float)var1.reloadTicks;
      if (getActivePerk(var0, var1, 0) == 103) {
         var4 *= 0.92F;
      }

      int var5 = getActivePerk(var0, var1, 1);
      if (var5 == 203) {
         var4 *= 0.88F;
      }

      if (var5 == 204) {
         var4 *= 0.75F;
      }

      if (GunDataHelper.getLong(var0, "d2_outlaw") > var2) {
         var4 *= 0.6F;
      }

      if (getActivePerk(var0, var1, 2) == 302 && GunDataHelper.getLong(var0, "d2_rh_exp") > var2) {
         int var6 = GunDataHelper.getInt(var0, "d2_rh_stk", 0);
         if (var6 > 0) {
            var4 *= 1.0F - (float)var6 * 0.07F;
         }
      }

      if (getActivePerk(var0, var1, 3) == 404 && GunDataHelper.getLong(var0, "d2_frz_exp") > var2 && var2 - GunDataHelper.getLong(var0, "d2_frz_st") >= 120L) {
         var4 *= 0.75F;
      }

      int var7 = Math.round(var4);
      if (var7 < 10) {
         var7 = 10;
      }

      return var7;
   }

   public static float computeSwappableDamageMult(ItemStack var0, WeaponSpec var1, LivingEntity var2, int var3, long var4) {
      float var6 = 1.0F;
      if (getActivePerk(var0, var1, 1) == 205) {
         var6 *= 1.06F;
      }

      if (getActivePerk(var0, var1, 2) == 306 && var4 >= GunDataHelper.getLong(var0, "d2_os_cd")) {
         GunDataHelper.setLong(var0, "d2_os_cd", var4 + 70L);
         if (var3 != 0) {
            var6 *= 1.1F;
         }
      }

      int var7 = getActivePerk(var0, var1, 3);
      if (var7 == 401) {
         if (GunDataHelper.getLong(var0, "d2_kc_exp") > var4) {
            var6 *= 1.25F;
         }
      } else if (var7 == 402) {
         if (GunDataHelper.getLong(var0, "d2_rmp_exp") > var4) {
            int var8 = GunDataHelper.getInt(var0, "d2_rmp_stk", 0);
            if (var8 > 0) {
               var6 *= 1.0F + (float)var8 * 0.11F;
            }
         }
      } else if (var7 == 403) {
         if (var2 != null && !(var2.getMaxHealth() < 50.0F)) {
            var6 *= 1.2F;
         }
      } else if (var7 == 404 && GunDataHelper.getLong(var0, "d2_frz_exp") > var4 && var4 - GunDataHelper.getLong(var0, "d2_frz_st") >= 120L) {
         var6 *= 1.15F;
      }

      return var6;
   }

   public static void onSwappableReloadComplete(ItemStack var0, WeaponSpec var1, long var2) {
      GunDataHelper.setInt(var0, "d2_rw_hits", 0);
      if (getActivePerk(var0, var1, 3) == 401 && GunDataHelper.getLong(var0, "d2_kc_arm_exp") > var2) {
         GunDataHelper.setLong(var0, "d2_kc_arm_exp", 0L);
         GunDataHelper.setLong(var0, "d2_kc_exp", var2 + 100L);
      }
   }

   public static void onSwappableWallHit(ServerLevel var0, ServerPlayer var1, ItemStack var2, WeaponSpec var3, Vec3 var4) {
      if (getActivePerk(var2, var3, 1) == 206) {
         ServerBallisticsEngine.triggerAoE(var0, var1, var4.x, var4.y, var4.z, 2.0, 3.5F, var3.element);
      }
   }

   public static void onSwappablePostHit(
      ServerLevel var0, ServerPlayer var1, ItemStack var2, WeaponSpec var3, LivingEntity var4, Vec3 var5, int var6, int var7, long var8
   ) {
      int var10 = getActivePerk(var2, var3, 1);
      int var11 = getActivePerk(var2, var3, 2);
      int var12 = getActivePerk(var2, var3, 3);
      int var13 = WeaponPerkRegistry.getEffectiveMagSize(var2, var3);
      if (var10 == 205) {
         var4.knockback(0.32, var1.getX() - var4.getX(), var1.getZ() - var4.getZ());
      }

      if (var12 == 404) {
         if (GunDataHelper.getLong(var2, "d2_frz_exp") <= var8) {
            GunDataHelper.setLong(var2, "d2_frz_st", var8);
         }

         GunDataHelper.setLong(var2, "d2_frz_exp", var8 + 120L);
      }

      if (var11 == 301) {
         if (var6 != 0 && var7 != 0) {
            GunDataHelper.setLong(var2, "d2_outlaw", var8 + 100L);
         }
      } else if (var11 == 302) {
         if (var6 != 0) {
            int var14;
            if (GunDataHelper.getLong(var2, "d2_rh_exp") <= var8) {
               var14 = 0;
            } else {
               var14 = GunDataHelper.getInt(var2, "d2_rh_stk", 0);
            }

            if (++var14 > 5) {
               var14 = 5;
            }

            GunDataHelper.setInt(var2, "d2_rh_stk", var14);
            GunDataHelper.setLong(var2, "d2_rh_exp", var8 + 80L);
         }
      } else if (var11 == 303) {
         if (var7 != 0) {
            int var17 = var13 * 18 / 100;
            if (var17 < 1) {
               var17 = 1;
            }

            int var15 = GunDataHelper.getMag(var2, var3) + var17;
            if (var15 > var13) {
               var15 = var13;
            }

            GunDataHelper.setInt(var2, "d2_mag", var15);
         }
      } else if (var11 == 304) {
         if (var6 != 0) {
            int var18;
            if (GunDataHelper.getLong(var2, "d2_fttc_exp") <= var8) {
               var18 = 0;
            } else {
               var18 = GunDataHelper.getInt(var2, "d2_fttc", 0);
            }

            var18++;
            GunDataHelper.setLong(var2, "d2_fttc_exp", var8 + 60L);
            if (var18 >= 4) {
               GunDataHelper.setInt(var2, "d2_fttc", 0);
               int var23 = GunDataHelper.getMag(var2, var3) + 2;
               if (var23 > var13) {
                  var23 = var13;
               }

               GunDataHelper.setInt(var2, "d2_mag", var23);
            } else {
               GunDataHelper.setInt(var2, "d2_fttc", var18);
            }
         } else {
            GunDataHelper.setInt(var2, "d2_fttc", 0);
         }
      } else if (var11 == 305) {
         int var20 = GunDataHelper.getInt(var2, "d2_rw_hits", 0) + 1;
         if (GunDataHelper.getMag(var2, var3) <= 0 && var20 >= 2) {
            int var24 = var20 * 60 / 100;
            if (var24 < 1) {
               var24 = 1;
            }

            if (var24 > var13) {
               var24 = var13;
            }

            GunDataHelper.setInt(var2, "d2_mag", var24);
            GunDataHelper.setInt(var2, "d2_rw_hits", 0);
         } else {
            GunDataHelper.setInt(var2, "d2_rw_hits", var20);
         }
      }

      if (var12 == 401) {
         if (var7 != 0) {
            GunDataHelper.setLong(var2, "d2_kc_arm_exp", var8 + 90L);
            return;
         }
      } else if (var12 == 402) {
         if (var7 != 0) {
            int var21;
            if (GunDataHelper.getLong(var2, "d2_rmp_exp") <= var8) {
               var21 = 0;
            } else {
               var21 = GunDataHelper.getInt(var2, "d2_rmp_stk", 0);
            }

            if (++var21 > 3) {
               var21 = 3;
            }

            GunDataHelper.setInt(var2, "d2_rmp_stk", var21);
            GunDataHelper.setLong(var2, "d2_rmp_exp", var8 + 90L);
            return;
         }
      } else {
         if (var12 == 405) {
            ServerBallisticsEngine.triggerAoE(var0, var1, var5.x, var5.y, var5.z, 2.4, var3.baseDamage * 0.22F, var3.element);
            return;
         }

         if (var12 == 406 && var7 != 0) {
            ServerBallisticsEngine.triggerAoE(var0, var1, var4.getX(), var4.getY(), var4.getZ(), 4.2, 9.0F, var3.element);
         }
      }
   }

   public static void appendPerkTooltips(ItemStack var0, WeaponSpec var1, List var2) {
      int var3 = getActivePerk(var0, var1, 0);
      var2.add(Component.literal("▸ Barrel: ".concat(getPerkName(var3))).withStyle(ChatFormatting.GRAY));
      var3 = getActivePerk(var0, var1, 1);
      var2.add(Component.literal("▸ Magazine: ".concat(getPerkName(var3))).withStyle(ChatFormatting.GRAY));
      var3 = getActivePerk(var0, var1, 2);
      var2.add(Component.literal("▸ Trait 1: ".concat(getPerkName(var3))).withStyle(ChatFormatting.YELLOW));
      var3 = getActivePerk(var0, var1, 3);
      var2.add(Component.literal("▸ Trait 2: ".concat(getPerkName(var3))).withStyle(ChatFormatting.GOLD));
   }

   public static void serverTrySocketPerk(ServerPlayer var0, int var1, int var2, int var3) {
      Inventory var4 = var0.getInventory();
      ItemStack var5;
      if (var1 >= 0 && var1 < var4.getContainerSize()) {
         var5 = var4.getItem(var1);
      } else {
         var5 = var0.getItemInHand(InteractionHand.MAIN_HAND);
      }

      WeaponSpec var6;
      label34: {
         if (!var5.isEmpty() && var5.getItem() instanceof DestinyGunItem) {
            var6 = ((DestinyGunItem)var5.getItem()).spec;
            if (var2 >= 0 && var2 <= 3 && isValidPerkForWeapon(var6.index, var2, var3) && getActivePerk(var5, var6, var2) != var3) {
               if (var0.isCreative()) {
                  break label34;
               }

               Item var7 = (Item)ModRegistries.PLUNDERED_ETHER_ALLOY.get();
               Item var8 = Items.COPPER_INGOT;
               if (GunsmithRecipes.countInInventory(var0, var7) >= 1 && GunsmithRecipes.countInInventory(var0, var8) >= 2) {
                  GunsmithRecipes.consumeFromInventory(var0, var7, 1);
                  GunsmithRecipes.consumeFromInventory(var0, var8, 2);
                  break label34;
               }

               ServerBallisticsEngine.playSound(var0.level(), var0.getX(), var0.getY(), var0.getZ(), "block.note_block.bass", 0.8F, 0.65F);
            }
         }

         return;
      }

      setActivePerk(var5, var6, var2, var3);
      var0.containerMenu.broadcastChanges();
      ServerBallisticsEngine.playSound(var0.level(), var0.getX(), var0.getY(), var0.getZ(), "block.smithing_table.use", 0.9F, 1.35F);
   }
}
