"""Original 32-second seamless aurora-themed pad, wind and glass tones.
Deterministic DSP; no recordings or neural audio model. Requires numpy/scipy/ffmpeg.
"""
from pathlib import Path
import numpy as np
from scipy.io import wavfile
import subprocess,tempfile
rate=22050;duration=32;n=rate*duration;t=np.arange(n)/rate;u=np.arange(n)/n
rng=np.random.default_rng(8819)
def tone(hz):return np.sin(2*np.pi*round(hz*duration)*u)
signal=np.zeros(n)
for i,f in enumerate([110,164.8125,220,261.625,329.625,493.875]):
    breathe=.55+.23*np.sin(2*np.pi*(i%3+1)*u+i*.8)
    signal+=(tone(f)+.23*tone(f+1/duration)+.10*tone(f*2))*breathe*(.028 if i<3 else .014)
freq=np.fft.rfftfreq(n,1/rate);spectrum=rng.normal(size=len(freq))+1j*rng.normal(size=len(freq))
response=(1-np.exp(-(freq/45)**2))*np.exp(-(freq/2200)**2)/np.maximum(freq,30)**.5
spectrum*=response;spectrum[0]=0;spectrum[-1]=spectrum[-1].real
wind=np.fft.irfft(spectrum,n=n);wind/=np.std(wind)
signal+=wind*.018*(.6+.4*np.sin(2*np.pi*u+.5)**2)
for i,(peak,hz) in enumerate([(.13,659.25),(.37,783.99),(.62,987.77),(.84,587.33)]):
    distance=((u-peak+.5)%1)-.5;envelope=np.exp(-(distance/(.014+i*.002))**2)
    signal+=(tone(hz)+.15*tone(hz*2.7))*envelope*.018
signal*=.055/np.sqrt(np.mean(signal**2))
assert np.isfinite(signal).all() and np.max(np.abs(signal))<.65
out=Path(__file__).resolve().parents[1]/'fabric/src/main/resources/assets/donuttracker/sounds/ocean/aurora_drift.ogg'
with tempfile.TemporaryDirectory() as temp:
    wav=Path(temp)/'aurora.wav';wavfile.write(wav,rate,(signal*32767).astype(np.int16))
    subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4','-map_metadata','-1',str(out)],check=True)
print('Aurora drift: 32-second mono loop; RMS',round(float(np.sqrt(np.mean(signal**2))),4),'peak',round(float(np.max(np.abs(signal))),4))
