import subprocess
import pyautogui
import time
import pyperclip


def write_ans(read_path, write_path):
    with open(read_path, encoding="UTF-8") as file:
        input_string = list(file.read().split("\n"))
        input_list = [input_string[i].split(":") for i in range(len(input_string))]
        write_string = ""
        splitter = ","
        write_string += splitter
        for i in range(1000):
            write_string += str(i)
            write_string += splitter
        write_string += "\n"
        ls = []
        for i in input_list:
            if i[0] == "generate":
                ls.append([""] * 1000)
                ls[-1][0] = i[1]
                for j in range(int(i[2]) + 1, 1000):
                    ls[-1][j] = "e"
            elif i[0] == "end":
                index = 0
                for x in range(len(ls)):
                    if ls[x][0] == i[1]:
                        index = x
                for j in range(int(i[2]) + 2, 1000):
                    ls[index][j] = ""
            elif i[0] == "CPU":
                index = 0
                for x in range(len(ls)):
                    if ls[x][0] == i[1]:
                        index = x
                for j in range(int(i[2]) + 1, int(i[3]) + 2):
                    ls[index][j] = "r"
            elif i[0] == "I/O":
                index = 0
                for x in range(len(ls)):
                    if ls[x][0] == i[1]:
                        index = x
                for j in range(int(i[2]) + 1, int(i[3]) + 2):
                    ls[index][j] = "w"
        for i in ls:
            for j in i:
                write_string += j
                write_string += splitter
            write_string += "\n"
    with open(write_path, mode="w", encoding="UTF-8") as file:
        file.write(write_string)
    subprocess.run(["start", "dir", "/w"], shell=True)
    time.sleep(1)
    pyperclip.copy(write_path)
    pyautogui.hotkey("ctrl", "v")
    pyautogui.press("enter")


if __name__ == '__main__':
    write_ans(r"C:\Alnya\tmp\tmp.txt",
              r"C:\Alnya\tmp\ans.csv")
