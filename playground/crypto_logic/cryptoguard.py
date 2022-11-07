import os, subprocess, re
from math import e

class Cryptoguard:

    def run_source(self):
        os.environ['ANDROID_HOME'] = '/home/ahmedulkavi/Android/Sdk'
        os.environ['JAVA7_HOME'] = '/usr/lib/jvm/java-8-openjdk-amd64'
        os.environ['JAVA_HOME'] = '/usr/lib/jvm/java-8-openjdk-amd64'

    def run_cryptoguard(self, file_path):
        cmd = "java"

        cryptoguard_path = 'crypto_tools/cryptoguard.jar'
        apk_path = file_path

        self.run_source()

        temp = subprocess.Popen([cmd, '-jar', cryptoguard_path, '-in', 'apk', '-s', apk_path, '-m', 'L'], stdout = subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        output = temp.communicate()
        # output = temp.args
        # output = subprocess.list2cmdline(output)

        return output

    def check_cryptoguard_violations(self, report):
        low_weight = 1
        medium_weight = 2
        high_weight = 3

        non_standardised_score = 0

        total_violations = report.count('***Violated Rule ')

        res = [m.start() for m in re.finditer(re.escape('***Violated Rule '), report)]
        violation_count = []
        violation_rule_number = []
        summary_list = []
        summary = ""
        # report.count('***Violated Rule '),
        i = 0
        count = 0
        for elem in res:
            if i+1<len(res):
                count = report.count('***Found:', elem, res[i+1])
            else:
                count = report.count('***Found:', elem)

            violation_count.append(count)
            i+=1
            
        for i in range(0, len(violation_count)-1):
            if violation_count[i] == 0:
                violation_count[i] = 1

        for elem in res:
            i = elem + 17
            num = ""
            while(report[i]!=':'):
                num += report[i]
                i+=1

            num = int(num)
            violation_rule_number.append(num)

        cnt = 0
        for elem in res:
            risk = ''
            if (violation_rule_number[cnt]>=1 and violation_rule_number[cnt]<=7) or violation_rule_number[cnt]==16:
                risk = "High"
                non_standardised_score += high_weight * violation_count[cnt]

            elif violation_rule_number[cnt]>=8 and violation_rule_number[cnt]<=12:
                risk = "Medium"
                non_standardised_score += medium_weight * violation_count[cnt]

            else:
                risk = "Low"
                non_standardised_score += low_weight * violation_count[cnt]

            if risk=="High":
                summary += "<div style=\"color:red;\">"
            elif risk=="Medium":
                summary += "<div style=\"color:orange;\">"
            else:
                summary += "<div style=\"color:black;\">"

            i = elem + 3
            while(report[i]!='*'):
                summary += report[i]
                i+=1

            summary += "Risk: " + risk + "\n"
            summary += str(violation_count[cnt]) + " time(s)\n"
            summary +="</div>"

            summary_list.append(summary)
            summary = ""

            cnt+=1

        score = non_standardised_score*56/95

        # score = e ** non_standardised_score

        # score = (e ** non_standardised_score) / ((e ** non_standardised_score)+1)
        # score *= 100

        summary = "<pre style=\"font-family: Garamond, serif;font-size: 18px;\">"
        summary += "Total violations: " + str(total_violations) + "\n"
        summary += "\n\n"
        # summary += "Cryptoguard Score: " + str(score) + "%\n\n"
        i = 0
        for elem in summary_list:
            summary += elem
            if i!=len(summary_list)-1:
                summary += "\n"
            i+=1
        summary+="</pre>"

        return summary

        # return res, violation_count, summary_list

    def __init__(self):
        print("")