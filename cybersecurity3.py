from scapy.all import IP , ICMP, sr1

def ping(host):
    packet = IP(dst=host)/ICMP()
    response = sr1(packet,timeout=1,verbose=0)
    if response:
        return f"{host} is online"
    else:
        return f"{host} is offline"
    
    #example
    host_to_scan = "example.com"
    result = ping(host_to_scan)
    print(result)