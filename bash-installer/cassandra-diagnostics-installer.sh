#!/usr/bin/env bash

# Extracts payload directory and starts installer entry script.

export TMPDIR=$(mktemp -d /tmp/cassandra-diagnostics-installer.XXXXXX)

INSTALLER_ARCHIVE=`awk '/^__INSTALLER_ARCHIVE__/ {print NR + 1; exit 0; }' $0`

tail -n+$INSTALLER_ARCHIVE $0 | tar xz -C $TMPDIR > /dev/null

"$TMPDIR"/main.sh $@
rm -rf $TMPDIR

exit 0

# Installer archive will be appended here as binary.
__INSTALLER_ARCHIVE__
����X installer.tar �=kw�6���_��>�ݘ�(�q�Sg��J��D��#�=I��� �5M�$e�m��� � 	�a{�ݳ�"��`0�`u{�Ʊ#�z�eƉ7���o��p������4���g�-5({{;����[S��?���3�Q��u��<�;;��3R�w�K�i��!�7�z�[Vo���в���4���^�M��w�q��F�� q� &�i0H��
#�.��i���G�iXu/P*h`�j�r�n��t���Gg=��랜�����/��q���Z[[#���XeK@)q}�
T��`�]N#����.�t�I�}�hVi����s��i���qF��5߽?-�����i=�A�c�FUA��;=��7�~�ӄ�&n2�#b�5�Κ���fO����y�{�~۲6�OV9V�7���<�~wo\}��C�m��q
����AQ�����3@���!�Y�V�}���}r�{�<�������V��\��U��;�o��l��7�M�k�ã���^Y�9��`/��IH�&���9�ăț$�� ��iL.�����ƍ<���� ^b�u����VIq�=ўƅ�$�f=���&��=? Nr��Io��|���JY $����%��KP�*_qfN�ux���\`vFQx]61[��IDc`�G��,��Fl�3&G��S
���һdV_�dL�$����X�L�iB	V�|�nR�V-	`!ʔL�	���G �Eb�0sc3$�<�¼s��Sc߭/� �K��A�0ɨ�A�<���tp�9�sx:I�-.�N�Ƚ��"���p+rʸBH ��&�(�%��q���l��04�DB���֋��f�]��4̏
[����<Cc�i����u��P9��z[Zj�Q&e-�U�g]��Al�|&��oR��yn=D���!k� �,/	�2!�l��Kz��s/�q}/'�]��(<RWH�t���������!��i���d�9]H�k�F��֑����K)���i}X���5�'>ua��/��@$LA��#�p�M��5�y%�Y���p���C��Q�32���F�H��!�ٽ`2M	`�L��xc�F�1�8��(�${d��(�ê�lَ�3�PJ�*rB�W��K:�D�r��f�"  HW.[HWdA����!UJW�L|�r����!�l�a����M *-E;>��:�����o�pA��h� ��RN�J�Iy	��eD'\��-NϿ� ����d�	��VQ��T�NQGa/�ƚ��*�*�KMkq;�L���^��,� k�`H�1eb���)S�>�'q������'���zU�X�k�;�c�7�Ѕ1��S� �F2SG�TS莼���T��b[��WloQ[��2Eb�oJ��R�������9(�|���A�.�(x�y��hg�o��LG6k�������|9k2I�p͙�*޳��:��z0b3�K�)ac#ܺ>Ԧ9�<�*e�0�a��0�؆;qԎ�:3�P��N�N~�L���ý���v���m�y�����y��7k�ic�:N} �-���Z����R-q�N#?~H�G-,��S��50؃�����<w���*��e^�g2��ހ\���˥���#7�pkQ�WͳV�Z�$����C�
]%�{W����5��7�}�C�����P>�tpG�Sn��ˌp���p��,;&��j�AF�f�)���B����i^>d��Ȼi-1�� �Pɐ����$j���I�vh��L���p���j#��T�:���F/w+�����s@��v}���|�O=

f�`�#�P��91��;z��cH�,5�c7q����0�p8e��ꎮ���P�=��� ���VA����������ee���4�\��o�4��@�ۭ������(����c����irKi��F0�1�gy2<b�����!����1$�������ÛpX�s�_&,|�<����;@D��� Ti_0>m=�CE���M�5������Sy�����4v܊?�PPL�)ȭK�or���4�"�q� })��
�k7Q�呤��f[���ƎF��h���1=h��O�3��~���X��žD ��y�-o�BZ�m[���SS�|vB޲��;��@���^�w=���ei�ǭWo�������/��l���wg��Q2?y�#�w�����7��g�z������OQ�����|���;����:��v�}w����4˧۩A�_;P%���u�/��R�aq��6�Cwgq:���Y7�"f����@m䑏؋2}*�S�%�R��� ��x��P�]d}�na0 C�;�7� � mFS_��*R,�^҄��x�mK�l��#����I^�&��m;����j>�?�w���-��"��6F��u����l�d��
����q�'Hli�o�nj�Y��و�II�4y��9y����0U099�z9�?3���	�ʖ|���`���9�S��W@˜��o���,�%?J�k�Z�
rQd�C��B���S�זqF-c�qe6l��Ӂ2���{����I��SHxg�+"��4]/ ��y(0�߃��R~���V��U`%��'YW$�AJ���%}��Բ4@����יR�e�*�"\B{�"�ۧ��S�y�x���i/��^/�S�0�=�$aBs���Gtp<�`�� Vp�>P������bz,�K�_�.���C^������	7�`s���&�0*J����L ��D�,�E�}�r�q�ᐲ5���H��Bb`�bzO��X�1XO�ĎnFi�Mu(�����阨��ψ���we��^o<�9{�ݕ��I�����}203Ø"Py�z��V�B�}���k�4HxuPm���r��+��X\>{�ADe|�Vq��0�]_������d{�X�X�e��w�,�}�JnC�� �TЧ"@�'��Z��ox��v}/A��ѱ�V�L�6��~I؏��q����r(F�8J��,�*�z>��>�]��8�,EP��Y�W�JV���t8�<�9|���r@�\.	3J��"E:H�����M�α�wʺ�pRoi&����d���K(�;`�) Ł�{Æ�?_��D|\&c��<����`�F=7a`�#ω�IaM ��d����y6ZY�u�
˒p�C�w����_ �Mw0����O֪9/`:L�ޅ�� q�q���r6_h��ן+��/���ly }	�����i=��R��sYՎ����d�x�{�ƌv[��W[�"���*��I�L��*�R�c�	5N���26�ͅ�!,k�+��#��6;�`���hlO�(~�oVf������������V�ߓ���Ә����,�k@�àO�ս8�5O�\�C�U�srq~��*���kb�f��]���ys�8�Sޝ�֡�?�hUڝ���۷`��T4�gM��5Y{����{�w2�A8l�c�{��j�{���k����D���{>?�[�
�� >t���M�!
�i/��
�=DU�@�(e�>T~��0��6/��a�VA<�����,v����z=���G���'��o����<t�o0���pD����b����DxȆ�u�{���3���"���рSĨ���l`��"@C�
 �WY�ؑ�\c�>�Ky<:=*���z9�-i&rD���K�C���{C�c�O����d�&�Z/i�A���������2!żB!�y�|�
�_�)
��\{q��2-t�ż��� `>~$�k�9���3#5�+zǏl�'<+��y	i)[(Z%}�!��o	�[z�x��ɋ�{�2*�],�G�ܼ�J:(;������x�&=&�z���jJQ}�1�_�%�1���@`��� 
RX<���E�=6I�CgI�?�0ׄ�#�`�,WF�'��)���
@u�ك-<���D)Ƽ�>)r{H�zYE�T�:��_����Zu5j��&�eaq�Ӊ�r��Hd��s��8���;�lq�Ա�fg�~��?V?oo_���%[�KmuP;��z�
�x�DN#Ô�INh��e��[��e4fSy����Du�
aA��߆�c��Ѣ!�ƺ�ߠ��i/x�pp���?8:+uP�ʪ���VA$��LyS�V
�\�
FL!J����Ķ������B�TG��`��F0X9��R lءq�4\!Q>Ui�����C(L�`k]�%N�r�-0�bL��Ñ���y�v@�d7���8<͌pz�;BO��l��rˊshp�*��qb]���6s-ɻJ����L�����x�6!2�\TTf'��5 2�TU��B&L9S\�2|S�bAtE��7@4�#D4�;��X�,�1T��o�mf$�_åT���џ�̆~#\�pWn���F&_��&��A�Ar�U���K����/B��h.������]g�����}����}������70�����U?�b���˖*��]��L��^a3RR�wk� ��IV��������:uV��i���r);�T}�`� �)F��g��$��@.u�d*g��U��
�/U1پ1��5�x��"P�Y��c�S_��?wp��V`_�qEK��T��	�9pb�Vjy�^���_<i��r�����z���Z��_���D(�`RVϋ{����H�������*�2'gaF~�B+V�;�Ly�Mζ���i�Y��Ni�^u,��� ,ToTke�g'�K�#S1�љl"*r�];_00�^�/Uũ�&�.Dg]����tF�~\�����0�y�g�-��0�\Ie�X_��~��ǻf�ͯ�B��g�[B���Z�zY��Ihب�t4�3/o
.@�c��d5aS�wqB�E^kLR� ���&vَ�#�0�2�������o�4J銲��qH��)�7= NdB#�����,#���?��E����G?>G����"���S��߻P}��?A1����Uy�Rݾ;���|P˜���ncO�gow�����j�?I)����WO���)��[B����/\�w��[���lV*U��|��FʪN"�?�^�y�6� �%U��Z{_�������ۃ� fg���S�B �T���ղ���jO`o{�9m����/ՙ�|�>�������������������)%뿣3EIBW�*�
3���'����. Q�nN�����|v��G~M�g�e^�9M�k�[hi�N�~�C�*H�֗���b��@}o��)��e��E*Y�H�F���1� ����J ��S�O�de��!��r��n��%�8�-�\��b)2���uy�n�.�y��r�2I��O�����p��Rn2sY�o��a��LX�#	ƫ'�#*c|��i4�̙��q��-
����[mq����y	���o�����K��<C\y�'?�Q�ZBC�_��Ǝ�h�"t`�7����U0;���?�Aʤ	~8"�W�t��S2]M��ϱpC1�|��;U���y�x�`�r�B���tS�So�1ο��i	���)��'�`N1��,(�"%.+�9�<.B�&�����p"���R*Ռg]��\#ç;+��u�7�{��V����_��6�y�"X�e�C�{L��X�/�u2 �f_W��?4���ւ� �1����,c�F�\]F��?��ĿyPN3�F9� ��<�`�V������1���}gO�4j�x�ou�����?w��*3��M�@q�0�Po�j��b���״ZoZ�`���9Z��_M����{��4�̐zC�S�H�x��iz�~lkNT�<�-d/s�" ;�x�-��yn��
vkՖ���i�?�!����N�4�Q��;U��`��4���!���¨b?C)� ���Gļ`3��f2��Er��
3�Pt��o�Z{m��1ng�"7{�Ea�d<ˍ~�=�7����I���N�N��yw�e�  Kʷr��n�b��Jw�@�7��|�E`\�[i'����t���d(o�w_f��-��� ��V�|ᅠ���R�7XF��L$�l���S�T\�݂�p�K�y����@��*�yM�{;饞q�y�|�Ϲ����'��LA� �'"��)x��ɳһ/��ru��eP������2�TP�Ay�0���U�(��Qkn����U6���ʪ�ʪ�ʪ�ʪ�ʪ�ʪ�ʪ�ʪ�ʪ�ʪ�ʪ�ʪ�ʪ��s�0)�� �  