import subprocess
import pyautogui
import time
import WriteAns


def main():
    subprocess.run(["start", "dir", "/w"], shell=True)
    time.sleep(1)
    pyautogui.typewrite("javac schedule1.java")
    pyautogui.press("enter")
    time.sleep(1)
    pyautogui.typewrite("java schedule1 1108.csv")
    pyautogui.press("enter")
    time.sleep(5)
    WriteAns.write_ans(r"C:\Alnya\tmp\tmp.txt",
                       r"C:\Alnya\tmp\ans.csv")


if __name__ == '__main__':
    main()
