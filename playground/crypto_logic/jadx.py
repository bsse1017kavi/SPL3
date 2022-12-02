from genericpath import isdir
import os, re
import subprocess, shutil

count = 0
report_desc = ''
report_summary = ''
report = ''
# violated_files = []
rules = {}
solutions = {}
rule_exhausted = {}

def load_rules():
    global rules
    with open("playground/crypto_logic/sec_tool_rules.txt", "r") as rule_file:
        content = rule_file.read()
        for line in content.splitlines():
            if line[0]=='#':
                continue
            rules_dummy_list = line.split(":")
            rules_value_list = rules_dummy_list[1].split(",")
            rules[rules_dummy_list[0]] = rules_value_list
            solution = rules_dummy_list[2]
            solutions[rules_dummy_list[0]] = solution
            rule_exhausted[rules_dummy_list[0]] = False
            print(rules[rules_dummy_list[0]])
        rule_file.close()

def search_rule_violation(directory_path):
    global count, violated_files, report_desc, report_summary, rule_exhausted
    for file in os.listdir(directory_path):
        if os.path.isdir(directory_path + "/" + file):
            search_rule_violation(directory_path+"/"+file)
        
        else:
            with open(directory_path+"/"+file, "r") as f:
                content = f.read()

                new_lines = [m.start() for m in re.finditer(re.escape('\n'), content)]

                for key in rules:
                    index = content.find(key)

                    line_number = 0

                    if index!=-1:
                        # ind = content.find("\"SSL\"")
                        for violation_keyword in rules[key]:
                            indices = [m.start() for m in re.finditer(re.escape(violation_keyword), content, re.IGNORECASE)]
                            for ind in indices:
                                if ind!=-1:
                                    count+=1
                                    # violated_files.append(file)
                                    for i in range(len(new_lines)):
                                        if new_lines[i]>ind:
                                            line_number = i+1
                                            break

                                if not rule_exhausted[key]:
                                    report_summary += violation_keyword + " Rule violation\n"
                                    report_summary += "Solution: " + solutions[key] + "\n\n"
                                    rule_exhausted[key] = True

                                print("SSL Rule violation in file: " + file + " line no: " + str(line_number))
                                print("Line: " + content[new_lines[line_number-2]+1:new_lines[line_number-1]].strip())
                                if violation_keyword==" Random":
                                    report_desc += "Random" + " Rule violation in file: " + file + " line no: " + str(line_number) + "\n"
                                else:
                                    report_desc += violation_keyword + " Rule violation in file: " + file + " line no: " + str(line_number) + "\n"
                                report_desc += "Line: " + content[new_lines[line_number-2]+1:new_lines[line_number-1]].strip() + "\n"
                                report_desc += "Solution: " + solutions[key] + "\n\n"
            
            report_summary = report_summary.replace(" Random", "Random")
            report_desc = report_desc.replace("<button","<p")
            report_desc = report_desc.replace("</button>","</p>")
                            
            f.close()

    # print(count)
    # print(violated_files)


def decompile(file_path):
    global report, report_desc, count
    jadx_path = 'crypto_tools/jadx/bin/jadx'
    # jadx_path = '../../crypto_tools/jadx/bin/jadx'

    if os.path.isdir("out"):
        shutil.rmtree('out')

    subprocess.call([jadx_path, file_path, "-d", "out"])

    report = ""
    report_desc = ""
    count = 0

    with open("out/resources/AndroidManifest.xml", "r") as android_manifest:
        text = android_manifest.read()
        searchString = "package=\""
        index = text.find(searchString) + len(searchString)
        package_name = ""

        for i in range(index, len(text)):
            if text[i]=='\"':
                break
            
            package_name += text[i]

        package_name = package_name.replace(".", "/")
        # package_name = package_name.replace("\"","")

        print(package_name)

        load_rules()

        # for key in rules:
        #     print(rules[key])

        search_rule_violation("out/sources/"+package_name)

        android_manifest.close()

        report += "Total violations: " + str(count) + "\n\n"
        # report += report_desc
        report += report_summary

        if os.path.isdir("out"):
            shutil.rmtree('out')

        return report, report_desc