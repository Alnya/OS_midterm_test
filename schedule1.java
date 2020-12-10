import java.io.*;
import java.util.*;

// CPU:79 I/O:114
// CSV��^�u��؂�̃X�P�W���[���t�@�C����ǂݍ��ނ̂Ɏg��
// name �̓v���Z�X�̖��O
// priority �̓v���Z�X�̗D��x�A0���W���ŁA�������D��x�A������D��x�A���̃v���O�����ł͖��g�p
// start �̓v���Z�X�����������V�~�����[�V��������
// schedule �� CPU��������(�����C���f�b�N�X) �� I/O �҂�����(��C���f�b�N�X) �� CPU��������(�����C���f�b�N�X) �c �̌J��Ԃ��̔z��
// �Ō�� CPU��������(�����C���f�b�N�X) �� -1(��C���f�b�N�X) �Ńv���Z�X�I��
class proc_schedule {
    String name;
    int priority;
    int start;
    int[] schedule;
}

// �V�~�����[�V�������̃v���Z�X
// ps �̓t�@�C������ǂݍ��񂾃v���Z�X�̃X�P�W���[�����
// stage �́Aps.schedule �z��̂ǂ̃C���f�b�N�X�܂ŏ������i�񂾂����o����
class proc {
    proc_schedule ps;
    int stage;
}

public class schedule1 {
    public static final String SPLITTER = ","; // �X�P�W���[���t�@�C���̕�������
    public static String writeSentence = "";

    public static void main(String[] arg) {
        ArrayList<proc_schedule> schedules;
        if (arg.length != 1) {
            System.err.println("Usage: java " + Thread.currentThread().getStackTrace()[1].getClassName() + " <�X�P�W���[���t�@�C��>");
            System.exit(0);
        }
        schedules = readSchedulesWithSplit(arg[0], SPLITTER);        // �X�P�W���[���t�@�C���̓ǂݍ���
        printSchedules(schedules);                    // �ǂݍ��񂾃X�P�W���[���̕\��
        simulate(schedules);                        // �V�~�����[�V����
        WriteTmp writeTmp = new WriteTmp(writeSentence);
        writeTmp.write();
    }

    public static void simulate(ArrayList<proc_schedule> sches) {
        proc cpu0 = null;
        ArrayList<proc> waiting = new ArrayList<proc>();
        ArrayList<proc> ready = new ArrayList<proc>();
        proc p;
        proc_schedule ps;
        int simtime = 0;
        int nrunning = 0;
        int i;

        System.out.printf("�J�n�O\t�����O %3d, ���s����� %1d, ���s�\��� %3d, �҂���� %3d\n", sches.size(), nrunning, ready.size(), waiting.size());
        while (sches.size() > 0 || nrunning > 0 || waiting.size() > 0 || ready.size() > 0) {
            // �v���Z�X�̐�������
            if (sches.size() > 0) {
                ps = sches.get(0);
                while (ps.start == simtime) {
                    p = new proc();
                    p.ps = ps;
                    p.stage = 0;
                    ready.add(p);
                    // System.out.println("\t�v���Z�X�̐��� ");
                    writeSentence += "generate:" + p.ps.name + ":" + simtime + "\n";
                    System.out.println("generate:" + p.ps.name + ":" + simtime);

                    sches.remove(0);
                    if (sches.size() == 0) {
                        break;
                    }
                    ps = sches.get(0);
                }
            }

            // CPU�ւ̃f�B�X�p�b�`����
            // CPU���󂢂Ă���Ȃ�A
            // ���s�\��Ԃ̑҂��s��̐擪�v���Z�X��CPU�����蓖�ĂĎ��s����Ԃ�
            if (cpu0 == null && ready.size() > 0) {
                p = ready.get(0);
                ready.remove(0);
                cpu0 = p;
                nrunning++;
                // System.out.println("\t�f�B�X�p�b�`(���s�\��Ԃ�����s����Ԃֈڍs)");
                writeSentence += "CPU:" + p.ps.name + ":" + simtime + ":" + (simtime + p.ps.schedule[p.stage] - 1) + "\n";
                System.out.println("CPU:" + p.ps.name + ":" + simtime + "��" + (simtime + p.ps.schedule[p.stage] - 1));
            }

            // System.out.printf("%6d\t�����O %3d, ���s����� %1d, ���s�\��� %3d, �҂���� %3d\n", simtime, sches.size(), nrunning, ready.size(), waiting.size());

            // 1 simulation time �o��
            // CPU�Ŏ��s����Ԃ̃v���Z�X�̌���(stage)��CPU�������Ԃ�1���炷
            // I/O�����ő҂���Ԃ̃v���Z�X�̌���(stage)��I/O�҂����Ԃ�1���炷
            // ���s�\��Ԃ̃v���Z�X�͉������Ȃ�
            if (cpu0 != null) {
                cpu0.ps.schedule[cpu0.stage]--;
                if (cpu0.ps.schedule[cpu0.stage] < 0) {
                    System.out.println("\tError\t���s���̃v���Z�X�̃p�����[�^�ɃG���[���L��܂�");
                    cpu0.ps.schedule[cpu0.stage] = 0;
                }
            }
            for (i = 0; i < waiting.size(); i++) {
                p = waiting.get(i);
                p.ps.schedule[p.stage]--;
                if (p.ps.schedule[p.stage] < 0) {
                    System.out.println("\tError\t�҂���Ԃ̃v���Z�X�̃p�����[�^�ɃG���[���L��܂�");
                    p.ps.schedule[p.stage] = 0;
                }
            }

            // CPU�Ŏ��s����Ԃ̃v���Z�X�̌���(stage)��CPU�������Ԃ�0�ɂȂ��Ă�����A��(�҂���� or �I��)�ɐi�߂�
            if (cpu0 != null) {
                if (cpu0.ps.schedule[cpu0.stage] == 0) {
                    p = cpu0;
                    cpu0 = null;
                    nrunning--;
                    p.stage++;
                    if (p.ps.schedule[p.stage] > 0) {
                        waiting.add(p);
                        // System.out.println("\t���s����Ԃ���҂���Ԃֈڍs");
                        writeSentence += "I/O:" + p.ps.name + ":" + (simtime + 1) + ":" + (simtime + p.ps.schedule[p.stage]) + "\n";
                        System.out.println("I/O:" + p.ps.name + ":" + (simtime + 1) + "��" + (simtime + p.ps.schedule[p.stage]));
                    } else {
                        // System.out.println("\t�v���Z�X(" + p.ps.name + ")�̏I��");
                        writeSentence += "end:" + p.ps.name + ":" + simtime + "\n";
                        System.out.println("end:" + p.ps.name + ":" + simtime);
                    }
                }
            }

            // I/O�����ő҂���Ԃ̃v���Z�X�̌���(stage)��I/O�҂����Ԃ�0�ɂȂ��Ă�����A��(���s�\���)�ɐi�߂�
            for (i = 0; i < waiting.size(); i++) {            // ���� for ���Ɖ��� for ���Ƃ� waiting ���� ready �ւ̈ڍs����
                p = waiting.get(i);                    // ���� for ���́Awaiting ��I/O�҂����Ԃ� 0 �ɂȂ������̂� ready �ɒǉ�
                if (p.ps.schedule[p.stage] == 0) {            // ���� for ���́Awaiting ��I/O�҂����Ԃ� 0 �ɂȂ������̂��X�V�E�폜
                    // p.stage ++;					// �v���Z�X�����Ԃ�1�����ׂĂ��邪�A�폜�𓯎��ɂ���
                    // waiting.remove(i);				// �����E��΂�����������̂ŁA2�ɕ�����
                    ready.add(p);
                    // System.out.println("\t�҂���Ԃ�����s�\��Ԃֈڍs");
                    // System.out.println("���s�\��ԊJ�n:" + p.ps.name + ":" + simtime);
                }
            }
            for (i = waiting.size() - 1; i >= 0; i--) {
                p = waiting.get(i);
                if (p.ps.schedule[p.stage] == 0) {
                    p.stage++;
                    waiting.remove(i);
                }
            }

            simtime++;
        }
    }

    // �t�@�C������ǂݍ��񂾃X�P�W���[�������R���\�[���ɕ\������
    public static void printSchedules(ArrayList<proc_schedule> sches) {
        proc_schedule ps;
        int i, n;
        n = sches.size();
        System.out.println("�����\��̃v���Z�X�� = " + n);
        System.out.println("�v���Z�X��\t�D��x\t��������\t[ CPU����, IO����, ... , -1(�I��) ]");
        for (i = 0; i < n; i++) {
            ps = sches.get(i);
            System.out.printf("%-14s\t%3d\t%6d\t\t%s\n", ps.name, ps.priority, ps.start, Arrays.toString(ps.schedule));
        }
    }

    // �w�肳�ꂽ��؂蕶���Ńt�@�C������v���Z�X�̃X�P�W���[����ǂݍ���
    // �s�̐擪�� # �̎��̓R�����g�s�Ƃ��Ė�������
    // ���̑��̊e�s��
    //		�v���Z�X��, �v���Z�X��������, CPU��������, I/O�҂�����, �c, -1
    // �Ō�� -1 ���A�v���Z�X�I�����Ӗ�����
    // �v���Z�X���������̏����Ńt�@�C���ɏ�����Ă��邱�Ƃ��O��
    public static ArrayList<proc_schedule> readSchedulesWithSplit(String fname, String splitter) {
        ArrayList<proc_schedule> sches = new ArrayList<proc_schedule>();
        File file = new File(fname);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String text;

            while ((text = br.readLine()) != null) {
                proc_schedule p = null;
                int i, j, ns;
                String[] cols = text.split(splitter);
                if (cols.length > 2 && !text.matches("^#.*")) {
                    p = new proc_schedule();
                    i = 0;                            // cols �̃C���f�b�N�X
                    j = 0;                            // schedule �̃C���f�b�N�X
                    p.name = cols[i++];
                    p.priority = Integer.parseInt(cols[i++]);
                    p.start = Integer.parseInt(cols[i++]);
                    ns = cols.length - i;                    // schedule �̐�
                    if (Integer.parseInt(cols[cols.length - 1]) != -1) {        // �ŏI�v�f�� -1 �łȂ��Ȃ�
                        ns++;                            // -1 �̕��������₷
                    }
                    p.schedule = new int[ns];
                    for (; i < cols.length; i++, j++) {
                        p.schedule[j] = Integer.parseInt(cols[i]);
                    }
                    if (p.schedule[j - 1] != -1) {
                        p.schedule[j] = -1;
                    }
                    sches.add(p);
                    // System.out.println(p.start + " " + Arrays.toString(p.schedule));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sches;
    }
}
