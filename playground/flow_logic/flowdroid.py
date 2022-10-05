import xml.etree.cElementTree as et
import json, xmltodict, subprocess, os

class FlowDroid:
    report = ""

    def run_flowdroid(self, file_path):
        cmd = "java"

        flowdroid_path = 'flow_tool/soot-infoflow-cmd-jar-with-dependencies.jar'
        apk_path = file_path

        android_sdk_platform = '/home/ahmedulkavi/Android/Sdk/platforms'
        source_and_sink_path = 'flow_tool/SourcesAndSinks.txt'
        output_path = 'leak.xml'

        temp = subprocess.Popen([cmd, '-jar', flowdroid_path, '-a', apk_path, '-p', android_sdk_platform, '-s', source_and_sink_path, '-o', output_path], stdout = subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        output = temp.communicate()
        # output = temp.args
        # output = subprocess.list2cmdline(output)

        return output

    def get_leak_report(self):
        global report

        leak_report = ""

        with open("leak.xml") as xml_file:
            data_dict = xmltodict.parse(xml_file.read())
            
        xml_file.close()

        os.remove("leak.xml")

        json_data = json.dumps(data_dict)

        json_mapping = json.loads(json_data)
        dataflowResults = json_mapping["DataFlowResults"]
        key = "Results"

        if key in dataflowResults:
            results = dataflowResults["Results"]
            result = results["Result"]
            # print(result)

            leak_count = len(result)

            leak_report += "<pre style=\"font-family: Garamond, serif;font-size: 16px;\">"

            leak_report += "Total leaks: " + str(leak_count) + "\n\n"

            for elem in result:
                # leak_report += self.concat_dashes()
                leak_report += "<div class=\"card shadow p-3 mb-5 bg-white rounded\" style=\"margin: 10px;\">"
                leak_report += "<div class=\"card-body\">"
                self.report = ""
                self.pretty(elem)
                leak_report += self.report
                # leak_report += self.concat_dashes()
                leak_report += "</div>"
                leak_report += "<br>"
                leak_report += "</div>"

            leak_report = leak_report.replace("@", "")
            leak_report = leak_report.replace("<", "\"")
            leak_report = leak_report.replace(">", "\"")

            leak_report = leak_report.replace("\"pre style=\"font-family: Garamond, serif;font-size: 16px;\"\"", "<pre style=\"font-family: Garamond, serif;font-size: 16px;\">")

            leak_report = leak_report.replace("\"div class=\"card shadow p-3 mb-5 bg-white rounded\" style=\"margin: 10px;\"\"", "<div class=\"card shadow p-3 mb-5 bg-white rounded\" style=\"margin: 10px;\">")
            leak_report = leak_report.replace("\"div class=\"card-body\"\"", "<div class=\"card-body\">")
            leak_report = leak_report.replace("\"/div\"", "</div>")
            leak_report = leak_report.replace("\"br\"", "<br>")

            leak_report += "</pre>"

        else:
            leak_report += "File has no leaks"

        

        # print(leak_report)

        # with open("data.json", "w") as json_file:
        #     json_file.write(json_data)

        # json_file.close()

        # tree=et.fromstring(sxml)

        # for el in tree.findall('Result'):
        #     print('-------------------')
        #     for ch in el.getchildren():
        #         print('{:>15}: {:<30}'.format(ch.tag, ch.text))

        # print(json_data)

        # print("Hello")

        return leak_report

    def pretty(self, d, indent=0):
        global report
        for key, value in d.items():
            self.report += '\t' * indent + str(key) + ": "
            if isinstance(value, dict):
                self.report += "\n"
                self.pretty(value, indent+1)
            else:
                self.report += str(value) + "\n"
        

    def concat_dashes(self):
        output = ""
        for i in range(130):
            output += "-"
        return output + "\n"

    def __init__(self):
        print("")