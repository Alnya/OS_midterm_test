import subprocess
import pyautogui
import time
import WriteAns
from settings import get_txt_path
from settings import get_ans_path


def main():
    subprocess.run(["start", "dir", "/w"], shell=True)
    time.sleep(1)
    pyautogui.typewrite("javac schedule1.java")
    pyautogui.press("enter")
    time.sleep(1)
    pyautogui.typewrite("java schedule1 1108.csv")
    pyautogui.press("enter")
    time.sleep(5)
    WriteAns.write_ans(get_txt_path(), get_ans_path())


if __name__ == '__main__':
    main()
