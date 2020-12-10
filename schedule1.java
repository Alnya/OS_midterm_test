import java.io.*;
import java.util.*;

// CPU:79 I/O:114
// CSVやタブ区切りのスケジュールファイルを読み込むのに使う
// name はプロセスの名前
// priority はプロセスの優先度、0が標準で、負が高優先度、正が低優先度、このプログラムでは未使用
// start はプロセスが生成されるシミュレーション時刻
// schedule は CPU処理時間(偶数インデックス) → I/O 待ち時間(奇数インデックス) → CPU処理時間(偶数インデックス) … の繰り返しの配列
// 最後は CPU処理時間(偶数インデックス) → -1(奇数インデックス) でプロセス終了
class proc_schedule {
    String name;
    int priority;
    int start;
    int[] schedule;
}

// シミュレーション中のプロセス
// ps はファイルから読み込んだプロセスのスケジュール情報
// stage は、ps.schedule 配列のどのインデックスまで処理が進んだかを覚える
class proc {
    proc_schedule ps;
    int stage;
}

public class schedule1 {
    public static final String SPLITTER = ","; // スケジュールファイルの分割文字
    public static String writeSentence = "";

    public static void main(String[] arg) {
        ArrayList<proc_schedule> schedules;
        if (arg.length != 1) {
            System.err.println("Usage: java " + Thread.currentThread().getStackTrace()[1].getClassName() + " <スケジュールファイル>");
            System.exit(0);
        }
        schedules = readSchedulesWithSplit(arg[0], SPLITTER);        // スケジュールファイルの読み込み
        printSchedules(schedules);                    // 読み込んだスケジュールの表示
        simulate(schedules);                        // シミュレーション
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

        System.out.printf("開始前\t生成前 %3d, 実行中状態 %1d, 実行可能状態 %3d, 待ち状態 %3d\n", sches.size(), nrunning, ready.size(), waiting.size());
        while (sches.size() > 0 || nrunning > 0 || waiting.size() > 0 || ready.size() > 0) {
            // プロセスの生成処理
            if (sches.size() > 0) {
                ps = sches.get(0);
                while (ps.start == simtime) {
                    p = new proc();
                    p.ps = ps;
                    p.stage = 0;
                    ready.add(p);
                    // System.out.println("\tプロセスの生成 ");
                    writeSentence += "generate:" + p.ps.name + ":" + simtime + "\n";
                    System.out.println("generate:" + p.ps.name + ":" + simtime);

                    sches.remove(0);
                    if (sches.size() == 0) {
                        break;
                    }
                    ps = sches.get(0);
                }
            }

            // CPUへのディスパッチ処理
            // CPUが空いているなら、
            // 実行可能状態の待ち行列の先頭プロセスにCPUを割り当てて実行中状態へ
            if (cpu0 == null && ready.size() > 0) {
                p = ready.get(0);
                ready.remove(0);
                cpu0 = p;
                nrunning++;
                // System.out.println("\tディスパッチ(実行可能状態から実行中状態へ移行)");
                writeSentence += "CPU:" + p.ps.name + ":" + simtime + ":" + (simtime + p.ps.schedule[p.stage] - 1) + "\n";
                System.out.println("CPU:" + p.ps.name + ":" + simtime + "→" + (simtime + p.ps.schedule[p.stage] - 1));
            }

            // System.out.printf("%6d\t生成前 %3d, 実行中状態 %1d, 実行可能状態 %3d, 待ち状態 %3d\n", simtime, sches.size(), nrunning, ready.size(), waiting.size());

            // 1 simulation time 経過
            // CPUで実行中状態のプロセスの現在(stage)のCPU処理時間を1減らす
            // I/O処理で待ち状態のプロセスの現在(stage)のI/O待ち時間を1減らす
            // 実行可能状態のプロセスは何もしない
            if (cpu0 != null) {
                cpu0.ps.schedule[cpu0.stage]--;
                if (cpu0.ps.schedule[cpu0.stage] < 0) {
                    System.out.println("\tError\t実行中のプロセスのパラメータにエラーが有ります");
                    cpu0.ps.schedule[cpu0.stage] = 0;
                }
            }
            for (i = 0; i < waiting.size(); i++) {
                p = waiting.get(i);
                p.ps.schedule[p.stage]--;
                if (p.ps.schedule[p.stage] < 0) {
                    System.out.println("\tError\t待ち状態のプロセスのパラメータにエラーが有ります");
                    p.ps.schedule[p.stage] = 0;
                }
            }

            // CPUで実行中状態のプロセスの現在(stage)のCPU処理時間が0になっていたら、次(待ち状態 or 終了)に進める
            if (cpu0 != null) {
                if (cpu0.ps.schedule[cpu0.stage] == 0) {
                    p = cpu0;
                    cpu0 = null;
                    nrunning--;
                    p.stage++;
                    if (p.ps.schedule[p.stage] > 0) {
                        waiting.add(p);
                        // System.out.println("\t実行中状態から待ち状態へ移行");
                        writeSentence += "I/O:" + p.ps.name + ":" + (simtime + 1) + ":" + (simtime + p.ps.schedule[p.stage]) + "\n";
                        System.out.println("I/O:" + p.ps.name + ":" + (simtime + 1) + "→" + (simtime + p.ps.schedule[p.stage]));
                    } else {
                        // System.out.println("\tプロセス(" + p.ps.name + ")の終了");
                        writeSentence += "end:" + p.ps.name + ":" + simtime + "\n";
                        System.out.println("end:" + p.ps.name + ":" + simtime);
                    }
                }
            }

            // I/O処理で待ち状態のプロセスの現在(stage)のI/O待ち時間が0になっていたら、次(実行可能状態)に進める
            for (i = 0; i < waiting.size(); i++) {            // この for 文と下の for 文とで waiting から ready への移行処理
                p = waiting.get(i);                    // この for 文は、waiting でI/O待ち時間が 0 になったものを ready に追加
                if (p.ps.schedule[p.stage] == 0) {            // 下の for 文は、waiting でI/O待ち時間が 0 になったものを更新・削除
                    // p.stage ++;					// プロセスを順番に1つずつ調べているが、削除を同時にやると
                    // waiting.remove(i);				// 抜け・飛ばしが発生するので、2つに分けた
                    ready.add(p);
                    // System.out.println("\t待ち状態から実行可能状態へ移行");
                    // System.out.println("実行可能状態開始:" + p.ps.name + ":" + simtime);
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

    // ファイルから読み込んだスケジュール情報をコンソールに表示する
    public static void printSchedules(ArrayList<proc_schedule> sches) {
        proc_schedule ps;
        int i, n;
        n = sches.size();
        System.out.println("処理予定のプロセス数 = " + n);
        System.out.println("プロセス名\t優先度\t生成時刻\t[ CPU時間, IO時間, ... , -1(終了) ]");
        for (i = 0; i < n; i++) {
            ps = sches.get(i);
            System.out.printf("%-14s\t%3d\t%6d\t\t%s\n", ps.name, ps.priority, ps.start, Arrays.toString(ps.schedule));
        }
    }

    // 指定された区切り文字でファイルからプロセスのスケジュールを読み込み
    // 行の先頭が # の時はコメント行として無視する
    // その他の各行は
    //		プロセス名, プロセス生成時刻, CPU処理時間, I/O待ち時間, …, -1
    // 最後の -1 が、プロセス終了を意味する
    // プロセス生成時刻の昇順でファイルに書かれていることが前提
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
                    i = 0;                            // cols のインデックス
                    j = 0;                            // schedule のインデックス
                    p.name = cols[i++];
                    p.priority = Integer.parseInt(cols[i++]);
                    p.start = Integer.parseInt(cols[i++]);
                    ns = cols.length - i;                    // schedule の数
                    if (Integer.parseInt(cols[cols.length - 1]) != -1) {        // 最終要素が -1 でないなら
                        ns++;                            // -1 の分だけ増やす
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
